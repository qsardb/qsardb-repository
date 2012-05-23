package org.dspace.content.packager;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;

public class QdbDisseminator implements PackageDisseminator {

	@Override
	public String getParameterHelp(){
		return null;
	}

	@Override
	public void disseminate(Context context, DSpaceObject object, PackageParameters parameters, File file) throws AuthorizeException, SQLException, IOException {
		Item item = (Item)object;

		Bitstream bitstream = QdbUtil.getOriginalBitstream(context, item);

		if(!file.exists()){
			PackageUtils.createFile(file);
		}

		InputStream is = bitstream.retrieve();

		try {
			OutputStream os = new FileOutputStream(file);

			try {
				Utils.copy(is, os);
			} finally {
				os.close();
			}
		} finally {
			is.close();
		}
	}

	@Override
	public List<File> disseminateAll(Context context, DSpaceObject object, PackageParameters parameters, File file){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMIMEType(PackageParameters parameters){
		return "application/x-zip"; // XXX
	}
}