package org.dspace.ctask.general;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.OpType;
import org.dspace.content.QdbModelUtil;
import org.qsardb.cargo.matrix.MahalanobisDistanceCargo;
import org.qsardb.cargo.matrix.MahalanobisDistanceValuesCargo;
import org.qsardb.cargo.pmml.FieldNameUtil;
import org.qsardb.cargo.pmml.PMMLCargo;
import org.qsardb.cargo.rds.RDSCargo;
import org.qsardb.evaluation.Evaluator;
import org.qsardb.evaluation.PMMLEvaluator;
import org.qsardb.model.Compound;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;
import org.qsardb.model.Property;

public class QdbMahalanobisDistanceCalculator extends QdbModelTask {

	@Override
	public boolean accept(Model model) {

		if(model.hasCargo(RDSCargo.class) || model.hasCargo(PMMLCargo.class)) {
			return !hasDistance(model, MahalanobisDistanceCargo.class);
		}

		return false;
	}

	@Override
	public boolean curate(Model model, Prediction training, Collection<Prediction> validations) throws Exception {
		Evaluator evaluator = QdbModelUtil.getEvaluator(model);

		if(evaluator != null) {
			evaluator.init();

			try {
				if(!requiresMahalanobisDistances(evaluator, model.getProperty())) {
					return false;
				}

				List<Descriptor> descriptors = evaluator.getDescriptors();

				MahalanobisDistanceCargo distanceCargo = model.getOrAddCargo(MahalanobisDistanceCargo.class);

				{
					Map<Compound, Double> distances = distanceCargo.prepare(getCompounds(training), descriptors);

					MahalanobisDistanceValuesCargo distanceValuesCargo = training.getOrAddCargo(MahalanobisDistanceValuesCargo.class);
					distanceValuesCargo.storeDoubleMap(convertMap(distances), QdbModelTask.format);
				}

				for(Prediction validation : validations) {
					Map<Compound, Double> distances = distanceCargo.predict(getCompounds(validation));

					MahalanobisDistanceValuesCargo distanceValuesCargo = validation.getOrAddCargo(MahalanobisDistanceValuesCargo.class);
					distanceValuesCargo.storeDoubleMap(convertMap(distances), QdbModelTask.format);
				}

				report("Curated Model \'" + model.getId() + "\'");

				return true;
			} finally {
				evaluator.destroy();
			}
		}

		return false;
	}

	static private boolean requiresMahalanobisDistances(Evaluator evaluator, Property property) {
		if(evaluator instanceof PMMLEvaluator) {
			PMMLEvaluator pmmlEvaluator = (PMMLEvaluator)evaluator;

			FieldName propField = FieldNameUtil.encodeProperty(property);
			for (DataField df : pmmlEvaluator.getModelManager().getDataDictionary().getDataFields()) {
				if (propField.equals(df.getName()) && df.getOptype() == OpType.CATEGORICAL) {
					return false;
				}
			}
		}

		return true;
	}
}