package org.dspace.content.packager;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.qsardb.model.*;
import org.qsardb.storage.zipfile.*;

import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.content.Collection;
import org.dspace.content.QdbUtil;
import org.dspace.core.*;

public class QdbPackager extends SelfNamedPlugin implements PackageIngester {

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

			QdbUtil.setTitle(item);

			QdbUtil.BitstreamData data = new QdbUtil.FileBitstreamData(file);

			QdbUtil.addOriginalBitstream(context, item, data);

			File internalFile = QdbUtil.optimize(file);

			try {
				QdbUtil.BitstreamData internalData = new QdbUtil.FileBitstreamData(internalFile){

					@Override
					public String getName(){
						return "archive.qdb";
					}
				};

				QdbUtil.addInternalBitstream(context, item, internalData);
			} finally {
				internalFile.delete();
			}

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

	static
	public String[] getPluginNames(){
		return QdbPackager.names;
	}

	private static final String[] names = {"QsarDB", "QDB"};
}