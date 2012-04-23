package org.dspace.submit.step;

import java.io.*;
import java.sql.*;

import javax.servlet.http.*;

import org.qsardb.model.*;

import org.dspace.app.util.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.content.QdbUtil;
import org.dspace.core.*;
import org.dspace.submit.*;

public class QdbOptimizeStep extends AbstractProcessingStep {

	@Override
	public int getNumberOfPages(HttpServletRequest request, SubmissionInfo submissionInfo){
		return 1;
	}

	@Override
	public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo submissionInfo) throws AuthorizeException, SQLException, IOException {
		Item item = submissionInfo.getSubmissionItem().getItem();

		Bitstream original = QdbUtil.getOriginalBitstream(context, item);

		File file = QdbUtil.loadFile(original);

		try {
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
		} catch(QdbException qe){
			throw new IOException(qe);
		} finally {
			file.delete();
		}

		context.commit();

		return STATUS_COMPLETE;
	}
}