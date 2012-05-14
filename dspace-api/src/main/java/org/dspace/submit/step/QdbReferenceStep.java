package org.dspace.submit.step;

import java.sql.*;

import javax.servlet.http.*;

import org.jbibtex.*;
import org.jbibtex.citation.*;

import org.apache.log4j.*;

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

		item.clearMetadata("dc", "title", null, null);

		String reference = null;

		BibTeXEntry entry = BibTeXUtil.toEntry(item);
		if(entry != null){

			try {
				reference = formatReference(entry);
			} catch(Exception e){
				logger.error("Failed to create reference", e);
			}
		}

		item.addMetadata("dc", "title", null, null, reference);

		item.update();

		context.commit();

		return STATUS_COMPLETE;
	}

	private String formatReference(BibTeXEntry entry){
		ReferenceFormatter formatter = new ReferenceFormatter(new ACSReferenceStyle());

		// Don't want to have the DOI as part of the title
		entry.removeField(BibTeXEntry.KEY_DOI);

		return formatter.format(entry, false, false);
	}

	private static final Logger logger = Logger.getLogger(QdbReferenceStep.class);
}