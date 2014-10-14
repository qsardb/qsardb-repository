package org.dspace.ctask.general;

import java.util.*;

import org.qsardb.cargo.matrix.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

import org.jpmml.manager.*;

import org.dspace.curate.*;
import org.jpmml.evaluator.RegressionModelEvaluator;

@Distributive
public class QdbLeverageCalculator extends QdbModelTask {

	@Override
	public boolean accept(Model model){

		if(model.hasCargo(PMMLCargo.class)){
			return !hasDistance(model, LeverageCargo.class);
		}

		return false;
	}

	@Override
	public boolean curate(Model model, Prediction training, Collection<Prediction> validations) throws Exception {
		Evaluator evaluator = org.dspace.content.QdbUtil.getEvaluator(model);

		if(evaluator != null){
			evaluator.init();

			try {

				if(!checkEvaluator(evaluator)){
					return false;
				}

				List<Descriptor> descriptors = evaluator.getDescriptors();

				LeverageCargo leverageCargo = model.getOrAddCargo(LeverageCargo.class);

				{
					Map<Compound, Double> leverages = leverageCargo.prepare(getCompounds(training), descriptors);

					LeverageValuesCargo leverageValuesCargo = training.getOrAddCargo(LeverageValuesCargo.class);
					leverageValuesCargo.storeDoubleMap(convertMap(leverages), QdbModelTask.format);
				}

				for(Prediction validation : validations){
					Map<Compound, Double> leverages = leverageCargo.predict(getCompounds(validation));

					LeverageValuesCargo leverageValuesCargo = validation.getOrAddCargo(LeverageValuesCargo.class);
					leverageValuesCargo.storeDoubleMap(convertMap(leverages), QdbModelTask.format);
				}

				report("Curated Model \'" + model.getId() + "\'");

				return true;
			} finally {
				evaluator.destroy();
			}
		}

		return false;
	}

	static
	private boolean checkEvaluator(Evaluator evaluator){

		if(evaluator instanceof PMMLEvaluator){
			PMMLEvaluator pmmlEvaluator = (PMMLEvaluator)evaluator;

			return checkModelManager(pmmlEvaluator.getModelManager());
		}

		return false;
	}

	static
	private boolean checkModelManager(ModelManager<?> modelManager){

		if(modelManager instanceof RegressionModelEvaluator){
			return true;
		}

		return false;
	}
}