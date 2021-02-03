/*
 *  Copyright (c) 2021 University of Tartu
 */
package org.dspace.content;

import it.jrc.ecb.qmrf.QMRF;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.qsardb.conversion.qmrf.QmrfUtil;
import org.xml.sax.SAXException;

/**
 * Support for QMRF reports.
 */
public class QmrfArchive {

	private static final BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();

	private QMRF qmrf;

	public QmrfArchive(Context context, Item item) {
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
}
