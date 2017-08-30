package org.dspace.submit.step;

import java.io.*;
import java.sql.*;
import java.util.List;

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
	public int doProcessing(final Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo submissionInfo) throws AuthorizeException, IOException, SQLException {


		final
		Item item = submissionInfo.getSubmissionItem().getItem();

		if (isComplete(item)) {
			return STATUS_COMPLETE;
		}

		QdbCallable<Object> callable = new QdbCallable<Object>(){

			@Override
			public Object call(Qdb qdb){
				QdbUtil.resetMetadata(context, item, qdb);

				return null;
			}
		};

		try {
			QdbUtil.invokeOriginal(context, item, callable);
		} catch(IOException ioe){
			throw ioe;
		} catch(Exception e){
			throw new RuntimeException(e);
		}

		itemService.update(context, item);
		context.dispatchEvents();

		return STATUS_COMPLETE;
	}

	private boolean isComplete(Item item){
		List<MetadataValue> bibtex = itemService.getMetadata(item, "bibtex", "entry", Item.ANY, Item.ANY);
		List<MetadataValue> qdb = itemService.getMetadata(item, "qdb", Item.ANY, Item.ANY, Item.ANY);
		return !bibtex.isEmpty() || !qdb.isEmpty();
	}
}
