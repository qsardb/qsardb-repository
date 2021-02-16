/*
 *  Copyright (c) 2021 University of Tartu
 */
package org.dspace.content;

import it.jrc.ecb.qmrf.AuthorRef;
import it.jrc.ecb.qmrf.EndpointRef;
import it.jrc.ecb.qmrf.ModelEndpoint;
import it.jrc.ecb.qmrf.QMRF;
import it.jrc.ecb.qmrf.QSARAlgorithm;
import it.jrc.ecb.qmrf.QSAREndpoint;
import it.jrc.ecb.qmrf.QSARGeneralInformation;
import it.jrc.ecb.qmrf.QSARIdentifier;
import it.jrc.ecb.qmrf.SoftwareRef;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.citation.ACSAuthorFormat;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.jsoup.Jsoup;
import org.qsardb.conversion.qmrf.QmrfUtil;
import org.xml.sax.SAXException;

/**
 * Support for QMRF reports.
 */
public class QmrfArchive {

	private static final ItemService itemService = ContentServiceFactory.getInstance().getItemService();
	private static final BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();

	private QMRF qmrf;
	private final Context context;
	private final Item item;

	public QmrfArchive(Context context, Item item) {
		this.context = context;
		this.item = item;
		
		for (Bitstream bs : findBitstreams(context, item, Constants.DEFAULT_BUNDLE_NAME, "QMRF")) {
			try {
				qmrf = parse(context, bs);
				return;
			} catch (IOException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}

		throw new IllegalArgumentException("Unable to find the QMRF file");
	}

	public static boolean containsQmrf(Context context, Item item) {
		return !findBitstreams(context, item, Constants.DEFAULT_BUNDLE_NAME, "QMRF").isEmpty();
	}

	public static boolean containsPdf(Context context, Item item) {
		return !findBitstreams(context, item, Constants.DEFAULT_BUNDLE_NAME, "Adobe PDF").isEmpty();
	}

	static boolean isQmrf(Context context, Bitstream bitstream) {
		try {
			parse(context, bitstream);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	private static QMRF parse(Context context, Bitstream bitstream) throws IOException {
		File file;
		try {
			file = bitstreamService.getFile(context, bitstream);
		} catch (IOException | SQLException | AuthorizeException ex) {
			throw new IOException("Can't access QMRF: "+bitstream.getID(), ex);
		}
		
		try {
			InputStream is = new FileInputStream(file);
			return QmrfUtil.loadQmrf(is);
		} catch (FileNotFoundException | JAXBException | SAXException ex) {
			throw new IOException("Can't parse QMRF: " + file + " " + bitstream.getID(), ex);
		}
	}

	private static ArrayList<Bitstream> findBitstreams(Context context, Item item, String bundleName, String shortDescription) {
		ArrayList<Bitstream> result = new ArrayList<>();
		for (Bundle bundle : item.getBundles(bundleName)) {
			for (Bitstream bs : bundle.getBitstreams()) {
				try {
					BitstreamFormat format = bs.getFormat(context);
					if (shortDescription.equals(format.getShortDescription())) {
						result.add(bs);
					}
				} catch (SQLException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		return result;
	}

	public void collectMetadata() throws SQLException {
		List<MetadataValue> issued = itemService.getMetadata(item, MetadataSchema.DC_SCHEMA, "date", "issued", Item.ANY);
		if (issued.isEmpty()) {
			itemService.addMetadata(context, item, MetadataSchema.DC_SCHEMA, "date", "issued", null, "today");
		}

		collectBibTeXMetadata();

		collectPropertyMetadata();
		collectDescriptorMetadata();
		collectModelMetadata();
		collectPredictionMetadata();
	}

	private void collectBibTeXMetadata() throws SQLException {
		itemService.addMetadata(context, item, "bibtex", "entry", null, null, "techreport");

		QSARGeneralInformation inf = qmrf.getQMRFChapters().getQSARGeneralInformation();
		for (AuthorRef ref : inf.getModelAuthors().getAuthorRef()) {
			String name = ACSAuthorFormat.normalize(ref.getIdref().getName());
			itemService.addMetadata(context, item, "bibtex", "entry", "author", null, name);
		}

		String date = strip(inf.getModelDate().getContent());
		String year = date.replaceFirst(".*(\\d{4}).*", "$1");
		itemService.addMetadata(context, item, "bibtex", "entry", "year", null, year);

		String title = strip(qmrf.getQMRFChapters().getQSARIdentifier().getQSARTitle().getContent());
		itemService.addMetadata(context, item, "bibtex", "entry", "title", null, title);
	}

	private void collectPropertyMetadata() throws SQLException {
		QSAREndpoint endpointElement = qmrf.getQMRFChapters().getQSAREndpoint();

		LinkedHashSet<String> endpoints = new LinkedHashSet<>();
		ModelEndpoint modelEndpoint = endpointElement.getModelEndpoint();
		for (EndpointRef ref : modelEndpoint.getEndpointRef()) {
			endpoints.add(ref.getIdref().getName());
		}
		if (!endpoints.isEmpty()) {
			itemService.addMetadata(context, item, "qdb", "property", "endpoint", null, new ArrayList(endpoints));
		}

		String species = strip(endpointElement.getModelSpecies().getContent());
		if (!species.isEmpty()) {
			itemService.addMetadata(context, item, "qdb", "property", "species", null, species);
		}
	}

	private void collectDescriptorMetadata() throws SQLException {
		LinkedHashSet<String> apps = new LinkedHashSet<>();
		QSARAlgorithm algo = qmrf.getQMRFChapters().getQSARAlgorithm();
		for (SoftwareRef ref : algo.getDescriptorsGenerationSoftware().getSoftwareRef()) {
			String name = ref.getIdref().getName();
			apps.add(name);
		}

		if (!apps.isEmpty()) {
			itemService.addMetadata(context, item, "qdb", "descriptor", "application", null, new ArrayList<>(apps));
		}
	}

	private void collectModelMetadata() throws SQLException {
		for (Bitstream bs : findBitstreams(context, item, Constants.DEFAULT_BUNDLE_NAME, "Adobe PDF")) {
			String value = bs.getName().replaceFirst("(?i)\\.pdf$", "");
			itemService.addMetadata(context, item, "qdb", "model", "qmrf", null, value);
			break;
		}
	}

	private void collectPredictionMetadata() throws SQLException {
		LinkedHashSet<String> apps = new LinkedHashSet<>();

		QSARIdentifier qid = qmrf.getQMRFChapters().getQSARIdentifier();
		for (SoftwareRef ref : qid.getQSARSoftware().getSoftwareRef()) {
			apps.add(ref.getIdref().getName());
		}

		if(!apps.isEmpty()){
			itemService.addMetadata(context, item, "qdb", "prediction", "application", null, new ArrayList<>(apps));
		}
	}

	private static String strip(String content) {
		if (content != null) {
			content = Jsoup.parse(content).text().trim();
			if (!content.isEmpty()) {
				return content;
			}
		}
		return "";
	}
}
