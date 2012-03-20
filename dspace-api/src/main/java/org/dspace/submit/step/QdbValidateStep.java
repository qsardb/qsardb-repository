package org.dspace.submit.step;

import javax.servlet.http.*;

import org.qsardb.model.*;

import org.dspace.app.util.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.submit.*;

public class QdbValidateStep extends AbstractProcessingStep {

	@Override
	public int getNumberOfPages(HttpServletRequest request, SubmissionInfo submissionInfo){
		return 1;
	}

	@Override
	public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo submissionInfo){
		Item item = submissionInfo.getSubmissionItem().getItem();

		final
		Level level = getLevel(request.getParameter("level"));

		QdbCallable<Boolean> callable = new QdbCallable<Boolean>(){

			@Override
			public Boolean call(Qdb qdb){
				// TODO

				return Boolean.TRUE;
			}
		};

		try {
			Boolean result = QdbUtil.invokeOriginal(context, item, callable);
			if(!result.booleanValue()){
				return STATUS_VALIDATION_ERROR;
			}

			return STATUS_COMPLETE;
		} catch(Exception e){
			return STATUS_QDB_ERROR;
		}
	}

	static
	private Level getLevel(String value){
		Level[] levels = Level.values();

		for(Level level : levels){

			if((level.getValue()).equals(value)){
				return level;
			}
		}

		return Level.BASIC;
	}

	static
	private enum Level {
		BASIC("basic"),
		INTERMEDIATE("intermediate"),
		ADVANCED("advanced"),
		;

		private String value = null;


		Level(String value){
			setValue(value);
		}

		public String getValue(){
			return this.value;
		}

		private void setValue(String value){
			this.value = value;
		}
	}

	public static final int STATUS_QDB_ERROR = 1;

	public static final int STATUS_VALIDATION_ERROR = 2;
}