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
import org.dspace.event.*;

public class QdbPackager extends SelfNamedPlugin implements PackageIngester, PackageDisseminator {

	@Override
	public String getParameterHelp(){
		return null;
	}

	@Override
	public DSpaceObject ingest(Context context, DSpaceObject parent, File file, PackageParameters parameters, String license) throws AuthorizeException, IOException, SQLException, PackageException {
		Collection collection = (Collection)parent;

		WorkspaceItem workspaceItem = WorkspaceItem.create(context, collection, false);

		Item item = workspaceItem.getItem();

		boolean success = false;

		try {
			deposit(context, collection, item, file, parameters, license);

			workspaceItem.update();

			InstallItem.installItem(context, workspaceItem);

			success = true;
		} finally {

			if(!success){
				workspaceItem.deleteAll();
			}

			context.commit();
		}

		return item;
	}

	@Override
	public List<DSpaceObject> ingestAll(Context context, DSpaceObject parent, File file, PackageParameters parameters, String license){
		throw new UnsupportedOperationException();
	}

	@Override
	public DSpaceObject replace(Context context, DSpaceObject object, File file, PackageParameters parameters) throws AuthorizeException, IOException, SQLException, PackageException {
		Item item = (Item)object;

		Collection collection = item.getOwningCollection();

		try {
			deposit(context, collection, item, file, parameters, null);

			item.update();

			// XXX
			item.inheritCollectionDefaultPolicies(collection);

			context.addEvent(new Event(Event.MODIFY | Event.MODIFY_METADATA, Constants.ITEM, item.getID(), item.getHandle()));
		} finally {
			context.commit();
		}

		return item;
	}

	@Override
	public List<DSpaceObject> replaceAll(Context context, DSpaceObject object, File file, PackageParameters parameters){
		throw new UnsupportedOperationException();
	}

	private void deposit(Context context, Collection collection, Item item, File file, PackageParameters parameters, String license) throws AuthorizeException, IOException, SQLException, PackageException {

		try {
			Qdb qdb = new Qdb(new ZipFileInput(file));

			try {
				QdbUtil.resetMetadata(item, qdb);
			} finally {
				qdb.close();
			}

			QdbUtil.resetTitle(item);

			QdbUtil.BitstreamData data = new QdbUtil.FileBitstreamData(file);

			QdbUtil.setOriginalBitstream(context, item, data);

			File internalFile = QdbUtil.optimize(file);

			try {
				QdbUtil.BitstreamData internalData = new QdbUtil.FileBitstreamData(internalFile){

					@Override
					public String getName(){
						return "archive.qdb";
					}
				};

				QdbUtil.setInternalBitstream(context, item, internalData);
			} finally {
				internalFile.delete();
			}

			if(PackageUtils.findDepositLicense(context, item) == null){
				PackageUtils.addDepositLicense(context, license, item, collection);
			}
		} catch(QdbException qe){
			throw new PackageException(qe);
		}
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

	static
	public String[] getPluginNames(){
		return QdbPackager.names;
	}

	private static final String[] names = {"QsarDB", "QDB"};
}