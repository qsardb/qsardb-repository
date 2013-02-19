package org.dspace.submit.step;

import java.sql.*;

import javax.servlet.http.*;

import org.dspace.app.util.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.submit.*;

public class QdbReferenceStep extends AbstractProcessingStep {

	@Override
	public int getNumberOfPages(HttpServletRequest request, SubmissionInfo submissionInfo){
		return 1;
	}

	@Override
	public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo submissionInfo) throws AuthorizeException, SQLException {
		Item item = submissionInfo.getSubmissionItem().getItem();

		QdbUtil.resetTitle(item);

		item.update();

		context.commit();

		return STATUS_COMPLETE;
	}
}