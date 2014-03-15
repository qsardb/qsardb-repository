package org.dspace.submit.step;

import java.util.*;

import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.rds.*;
import org.qsardb.model.*;
import org.qsardb.validation.*;

import org.apache.log4j.*;

import org.dspace.content.*;
import org.dspace.content.QdbUtil;
import org.dspace.core.Context;

public class QdbValidation {
	private Level level = Level.BASIC;

	public QdbValidation(String levelName) {
		if (levelName != null) {
			try {
				level = Level.valueOf(levelName.toUpperCase());
			} catch (IllegalArgumentException e) {
				// ignored, default level is BASIC
			}
		}
	}

	public ItemMessageCollector validate(Context context, Item item) {

		final ItemMessageCollector collector = new ItemMessageCollector(level.getValue());

		QdbCallable<ItemMessageCollector> callable = new QdbCallable<ItemMessageCollector>(){
			@Override
			public ItemMessageCollector call(Qdb qdb){
				return validate(qdb, collector);
			}
		};

		try {
			return QdbUtil.invokeOriginal(context, item, callable);
		} catch(Exception e){
			logger.error("Validation failed", e);
		}

		return collector;
	}

	private ItemMessageCollector validate(Qdb qdb, ItemMessageCollector collector){
		List<Validator<?>> validators = prepareValidators();

		for(Validator<?> validator : validators){
			validator.setCollector(collector);

			try {
				validator.run(qdb);
			} finally {
				validator.setCollector(null);
			}
		}
		return collector;
	}

	private List<Validator<?>> prepareValidators(){
		List<Validator<?>> result = new ArrayList<Validator<?>>();

		if((Level.BASIC).compareTo(level) <= 0){
			result.add(new BasicContainerValidator());
			result.add(new CompoundValidator(Scope.LOCAL));
			result.add(new PropertyValidator(Scope.LOCAL));
			result.add(new DescriptorValidator(Scope.LOCAL));
			result.add(new ModelValidator(Scope.LOCAL));
			result.add(new PredictionValidator(Scope.LOCAL));
			result.add(new PredictionRegistryValidator());
			result.add(new BasicCargoValidator(1024 * 1024){

				@Override
				@SuppressWarnings (
					value = {"rawtypes"}
				)
				public int getLimit(Cargo<?> cargo){
					Container container = cargo.getContainer();

					if(container instanceof Model){
						String id = cargo.getId();

						if((RDSCargo.ID).equals(id) || (PMMLCargo.ID).equals(id)){
							return 10 * 1024 * 1024;
						}
					}

					return super.getLimit(cargo);
				}
			});
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

	private static final Logger logger = Logger.getLogger(QdbValidation.class);
}