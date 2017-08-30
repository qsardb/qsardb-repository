package org.dspace.ctask.general;

import java.util.*;
import org.dspace.content.QdbModelUtil;

import org.qsardb.cargo.matrix.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.rds.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

public class QdbMahalanobisDistanceCalculator extends QdbModelTask {

	@Override
	public boolean accept(Model model){

		if(model.hasCargo(RDSCargo.class) || model.hasCargo(PMMLCargo.class)){
			return !hasDistance(model, MahalanobisDistanceCargo.class);
		}

		return false;
	}

	@Override
	public boolean curate(Model model, Prediction training, Collection<Prediction> validations) throws Exception {
		Evaluator evaluator = QdbModelUtil.getEvaluator(model);

		if(evaluator != null){
			evaluator.init();

			try {
				List<Descriptor> descriptors = evaluator.getDescriptors();

				MahalanobisDistanceCargo distanceCargo = model.getOrAddCargo(MahalanobisDistanceCargo.class);

				{
					Map<Compound, Double> distances = distanceCargo.prepare(getCompounds(training), descriptors);

					MahalanobisDistanceValuesCargo distanceValuesCargo = training.getOrAddCargo(MahalanobisDistanceValuesCargo.class);
					distanceValuesCargo.storeDoubleMap(convertMap(distances), QdbModelTask.format);
				}

				for(Prediction validation : validations){
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
}