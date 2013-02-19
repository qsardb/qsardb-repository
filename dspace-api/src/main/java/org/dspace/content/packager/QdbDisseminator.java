package org.dspace.content.packager;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.content.Collection;
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

		if(file.isDirectory()){
			file = new File(file, bitstream.getName());
		} // End if

		PackageUtils.createFile(file);

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
	public List<File> disseminateAll(Context context, DSpaceObject object, PackageParameters parameters, File dir) throws AuthorizeException, SQLException, IOException {
		Collection collection = (Collection)object;

		List<File> result = new ArrayList<File>();

		ItemIterator items = collection.getAllItems();

		try {
			while(items.hasNext()){
				Item item = items.next();

				Bitstream bitstream = QdbUtil.getOriginalBitstream(context, item);

				File file = new File(dir, bitstream.getName());
				if(file.exists()){
					throw new IOException("File " + file.getAbsolutePath() + " already exists");
				}

				PackageUtils.createFile(file);

				disseminate(context, item, parameters, file);

				result.add(file);
			}
		} finally {
			items.close();
		}

		return result;
	}

	@Override
	public String getMIMEType(PackageParameters parameters){
		return "application/x-zip"; // XXX
	}
}