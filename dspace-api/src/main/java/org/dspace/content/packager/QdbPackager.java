package org.dspace.content.packager;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.qsardb.model.*;
import org.qsardb.storage.zipfile.*;

import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.content.Collection;
import org.dspace.core.*;

public class QdbPackager extends SelfNamedPlugin implements PackageIngester, PackageDisseminator {

	@Override
	public String getParameterHelp(){
		return null;
	}

	@Override
	public DSpaceObject ingest(Context context, DSpaceObject parent, File file, PackageParameters parameters, String license) throws AuthorizeException, IOException, SQLException, PackageException {
		WorkspaceItem workspaceItem = WorkspaceItem.create(context, (Collection)parent, false);

		Item item = workspaceItem.getItem();

		boolean success = false;

		try {
			Qdb qdb = new Qdb(new ZipFileInput(file));

			try {
				QdbUtil.collectMetadata(item, qdb);
			} finally {
				qdb.close();
			}

			createBundle(context, item, file);

			item.update();

			workspaceItem.update();

			success = true;
		} catch(QdbException qe){
			throw new PackageException(qe);
		} finally {

			if(!success){
				workspaceItem.deleteAll();
			}

			context.commit();
		}

		return PackageUtils.finishCreateItem(context, workspaceItem, null, parameters);
	}

	@Override
	public List<DSpaceObject> ingestAll(Context context, DSpaceObject parent, File file, PackageParameters parameters, String license){
		throw new UnsupportedOperationException();
	}

	@Override
	public DSpaceObject replace(Context context, DSpaceObject parent, File file, PackageParameters parameters){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<DSpaceObject> replaceAll(Context context, DSpaceObject parent, File file, PackageParameters parameters){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMIMEType(PackageParameters parameters){
		return "application/x-zip";
	}

	@Override
	public void disseminate(Context context, DSpaceObject object, PackageParameters parameters, File file){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<File> disseminateAll(Context context, DSpaceObject object, PackageParameters parameters, File file){
		throw new UnsupportedOperationException();
	}

	private void createBundle(Context context, Item item, File file) throws AuthorizeException, IOException, SQLException {
		BitstreamFormat format = QdbUtil.getBitstreamFormat(context);

		Bundle bundle = item.createBundle(Constants.DEFAULT_BUNDLE_NAME);

		InputStream is = new FileInputStream(file);

		try {
			Bitstream data = bundle.createBitstream(is);
			data.setName(file.getName());
			data.setFormat(format);

			data.update();
		} finally {
			is.close();
		}
	}

	static
	public String[] getPluginNames(){
		return QdbPackager.names;
	}

	private static final String[] names = {"QsarDB", "QDB"};
}