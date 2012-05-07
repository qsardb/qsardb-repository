package org.dspace.submit.step;

import java.util.*;

import javax.servlet.http.*;

import org.qsardb.model.*;
import org.qsardb.validation.*;

import org.apache.log4j.*;

import org.dspace.app.util.*;
import org.dspace.content.*;
import org.dspace.content.QdbUtil;
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

		final
		ItemMessageCollector collector = new ItemMessageCollector(level.getValue());

		QdbCallable<Object> callable = new QdbCallable<Object>(){

			@Override
			public Object call(Qdb qdb){
				validate(qdb, level, collector);

				return null;
			}
		};

		try {
			QdbUtil.invokeOriginal(context, item, callable);

			ItemMessageCollector.store(item, collector);

			if(collector.hasErrors()){
				return STATUS_VALIDATION_ERROR;
			}

			return STATUS_COMPLETE;
		} catch(Exception e){
			logger.error("Validation failed", e);

			return STATUS_QDB_ERROR;
		}
	}

	private void validate(Qdb qdb, Level level, MessageCollector collector){
		List<Validator<?>> validators = prepareValidators(level);

		for(Validator<?> validator : validators){
			validator.setCollector(collector);

			try {
				validator.run(qdb);
			} finally {
				validator.setCollector(null);
			}
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
	private List<Validator<?>> prepareValidators(Level level){
		List<Validator<?>> result = new ArrayList<Validator<?>>();

		if((Level.BASIC).compareTo(level) <= 0){
			result.add(new BasicContainerValidator());
			result.add(new CompoundValidator(Scope.LOCAL));
			result.add(new PropertyValidator(Scope.LOCAL));
			result.add(new DescriptorValidator(Scope.LOCAL));
			result.add(new ModelValidator(Scope.LOCAL));
			result.add(new PredictionValidator(Scope.LOCAL));
			result.add(new PredictionRegistryValidator());
			result.add(new BasicCargoValidator());
			result.add(new ValuesValidator());
			result.add(new UCUMValidator());
			result.add(new ReferencesValidator());
			result.add(new PMMLValidator());
			result.add(new BibTeXValidator());
			result.add(new BODOValidator());
		} // End if

		if((Level.INTERMEDIATE).compareTo(level) <= 0){
			result.add(new CompoundValidator(Scope.GLOBAL));
			result.add(new PropertyValidator(Scope.GLOBAL));
			result.add(new DescriptorValidator(Scope.GLOBAL));
			result.add(new ModelValidator(Scope.GLOBAL));
			result.add(new PredictionValidator(Scope.GLOBAL));
		} // End if

		if((Level.ADVANCED).compareTo(level) <= 0){
			result.add(new PredictionReproducibilityValidator());
		}

		return result;
	}

	static
	public enum Level {
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

	private static final Logger logger = Logger.getLogger(QdbValidateStep.class);
}