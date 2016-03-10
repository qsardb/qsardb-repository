package org.dspace.submit.step;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.*;

import org.apache.log4j.*;

import org.dspace.app.util.*;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.submit.*;

public class QdbValidateStep extends AbstractProcessingStep {

	@Override
	public int getNumberOfPages(HttpServletRequest request, SubmissionInfo submissionInfo){
		return 1;
	}

	@Override
	public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo submissionInfo){
		Item item = submissionInfo.getSubmissionItem().getItem();

		String level = request.getParameter("level");

		QdbValidation validator = new QdbValidation(level);
		ItemMessageCollector collector = validator.validate(context, item);

		try {
			ItemMessageCollector.store(item, collector);
			return getStatus(collector);
		} catch(IOException e){
			logger.error("Validation failed", e);
			return STATUS_QDB_ERROR;
		}
	}

	public static int getStatus(ItemMessageCollector collector){

		if(collector.hasErrors()){
			return STATUS_VALIDATION_ERROR;
		}

		return STATUS_COMPLETE;
	}

	public static void cleanup(Item item) {
		File tempFile = ItemMessageCollector.getFile(item);
		if(tempFile.isFile()){
			tempFile.delete();
		}
	}

	public static final int STATUS_QDB_ERROR = 1;

	public static final int STATUS_VALIDATION_ERROR = 2;

	private static final Logger logger = Logger.getLogger(QdbValidateStep.class);
}