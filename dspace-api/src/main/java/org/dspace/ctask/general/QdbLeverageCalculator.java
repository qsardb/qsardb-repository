package org.dspace.ctask.general;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.OpType;
import org.dspace.content.QdbModelUtil;
import org.dspace.curate.Distributive;
import org.jpmml.evaluator.RegressionModelEvaluator;
import org.qsardb.cargo.matrix.LeverageCargo;
import org.qsardb.cargo.matrix.LeverageValuesCargo;
import org.qsardb.cargo.pmml.FieldNameUtil;
import org.qsardb.cargo.pmml.PMMLCargo;
import org.qsardb.evaluation.Evaluator;
import org.qsardb.evaluation.PMMLEvaluator;
import org.qsardb.model.Compound;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;
import org.qsardb.model.Property;

@Distributive
public class QdbLeverageCalculator extends QdbModelTask {

	@Override
	public boolean accept(Model model) {
		if(model.hasCargo(PMMLCargo.class)) {
			return !hasDistance(model, LeverageCargo.class);
		}

		return false;
	}

	@Override
	public boolean curate(Model model, Prediction training, Collection<Prediction> validations) throws Exception {
		Evaluator evaluator = QdbModelUtil.getEvaluator(model);

		if(evaluator != null) {
			evaluator.init();

			try {
				if(!requiresLeverages(evaluator, model.getProperty())) {
					return false;
				}

				List<Descriptor> descriptors = evaluator.getDescriptors();

				LeverageCargo leverageCargo = model.getOrAddCargo(LeverageCargo.class);

				{
					Map<Compound, Double> leverages = leverageCargo.prepare(getCompounds(training), descriptors);

					LeverageValuesCargo leverageValuesCargo = training.getOrAddCargo(LeverageValuesCargo.class);
					leverageValuesCargo.storeDoubleMap(convertMap(leverages), QdbModelTask.format);
				}

				for(Prediction validation : validations) {
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

	static private boolean requiresLeverages(Evaluator evaluator, Property property) {
		if(evaluator instanceof PMMLEvaluator) {
			PMMLEvaluator pmmlEvaluator = (PMMLEvaluator)evaluator;

			FieldName propField = FieldNameUtil.encodeProperty(property);
			for (DataField df : pmmlEvaluator.getModelManager().getDataDictionary().getDataFields()) {
				if (propField.equals(df.getName()) && df.getOptype() == OpType.CATEGORICAL) {
					return false;
				}
			}

			return pmmlEvaluator.getModelManager() instanceof RegressionModelEvaluator;
		}

		return false;
	}
}