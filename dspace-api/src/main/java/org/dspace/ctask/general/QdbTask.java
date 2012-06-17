package org.dspace.ctask.general;

import java.io.*;
import java.sql.*;

import org.qsardb.model.*;
import org.qsardb.storage.directory.*;
import org.qsardb.storage.zipfile.*;

import org.apache.log4j.*;

import org.dspace.content.*;
import org.dspace.content.QdbUtil;
import org.dspace.core.*;
import org.dspace.curate.*;

abstract
public class QdbTask extends AbstractCurationTask {

	private Context context = null;


	abstract
	public boolean accept(Qdb qdb) throws IOException, QdbException;

	abstract
	public boolean curate(Qdb qdb) throws IOException, QdbException;

	@Override
	public int perform(DSpaceObject object) throws IOException {

		try {
			Context context = new Context();

			this.context = context;

			try {
				distribute(object);

				context.complete();

				return Curator.CURATE_SUCCESS;
			} finally {

				if(context.isValid()){
					context.abort();
				}

				this.context = null;
			}
		} catch(SQLException e){
			return Curator.CURATE_ERROR;
		}
	}

	@Override
	public void performItem(Item item) throws IOException {
		File dir = FileUtil.createTempDirectory();

		try {
			DirectoryStorage storage = null;

			Bitstream bitstream = QdbUtil.getInternalBitstream(this.context, item);

			File inputFile = QdbUtil.loadFile(bitstream);

			try {
				Qdb qdb = new Qdb(new ZipFileInput(inputFile));

				try {
					if(!accept(qdb)){
						return;
					}

					storage = new DirectoryStorage(dir);

					qdb.copyTo(storage);
				} finally {
					qdb.close();
				}
			} finally {
				inputFile.delete();
			}

			File outputFile = null;

			try {
				Qdb qdb = new Qdb(storage);

				try {
					curate(qdb);

					outputFile = QdbUtil.optimize(storage);
				} finally {
					qdb.close();
				}

				QdbUtil.setInternalBitstream(this.context, item, outputFile);

				item.update();
			} finally {

				if(outputFile != null){
					outputFile.delete();
				}
			}
		} catch(Exception e){
			logger.error("Failed to curate item " + item.getHandle(), e);
		} finally {
			FileUtil.deleteTempDirectory(dir);
		}
	}

	private static final Logger logger = Logger.getLogger(QdbTask.class);
}