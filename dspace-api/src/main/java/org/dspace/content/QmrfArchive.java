/*
 *  Copyright (c) 2021 University of Tartu
 */
package org.dspace.content;

import it.jrc.ecb.qmrf.AuthorRef;
import it.jrc.ecb.qmrf.QMRF;
import it.jrc.ecb.qmrf.QSARGeneralInformation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
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
		
		for (Bitstream bs : findBitstreams(context, item, "QMRF")) {
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
		return !findBitstreams(context, item, "QMRF").isEmpty();
	}

	public static boolean containsPdf(Context context, Item item) {
		return !findBitstreams(context, item, "Adobe PDF").isEmpty();
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

	private static ArrayList<Bitstream> findBitstreams(Context context, Item item, String shortDescription) {
		ArrayList<Bitstream> result = new ArrayList<>();
		for (Bundle bundle : item.getBundles(Constants.DEFAULT_BUNDLE_NAME)) {
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
