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

public class QdbCrosswalkStep extends AbstractProcessingStep {

	@Override
	public int getNumberOfPages(HttpServletRequest request, SubmissionInfo submissionInfo){
		return 1;
	}

	@Override
	public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo submissionInfo) throws AuthorizeException, IOException, SQLException {

		if(SubmissionUtil.isComplete(this, submissionInfo)){
			return STATUS_COMPLETE;
		}

		final
		Item item = submissionInfo.getSubmissionItem().getItem();

		QdbCallable<Object> callable = new QdbCallable<Object>(){

			@Override
			public Object call(Qdb qdb){
				QdbUtil.resetMetadata(item, qdb);

				return null;
			}
		};

		try {
			QdbUtil.invokeInternal(context, item, callable);
		} catch(IOException ioe){
			throw ioe;
		} catch(Exception e){
			throw new RuntimeException(e);
		}

		item.update();

		context.commit();

		return STATUS_COMPLETE;
	}
}