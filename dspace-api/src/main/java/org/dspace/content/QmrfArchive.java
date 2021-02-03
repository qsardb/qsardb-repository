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
import javax.xml.bind.JAXBException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Context;
import org.qsardb.conversion.qmrf.QmrfUtil;
import org.xml.sax.SAXException;

/**
 * Support for QMRF reports.
 */
public class QmrfArchive {

	private static final BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();

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
}
