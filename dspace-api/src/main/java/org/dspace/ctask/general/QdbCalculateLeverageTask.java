package org.dspace.ctask.general;

import java.io.*;
import java.text.*;
import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.cargo.matrix.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

import org.jpmml.manager.*;

import org.apache.log4j.*;

import org.dspace.curate.*;

@Distributive
public class QdbCalculateLeverageTask extends QdbTask {

	@Override
	public boolean accept(Qdb qdb){
		ModelRegistry models = qdb.getModelRegistry();

		for(Model model : models){

			if(accept(model)){
				return true;
			}
		}

		return false;
	}

	private boolean accept(Model model){

		if(model.hasCargo(PMMLCargo.class)){
			return !model.hasCargo(LeverageCargo.class);
		}

		return false;
	}

	@Override
	public boolean curate(Qdb qdb) throws IOException, QdbException {
		ModelRegistry models = qdb.getModelRegistry();
		PredictionRegistry predictions = qdb.getPredictionRegistry();

		boolean changed = false;

		for(Model model : models){
			Collection<Prediction> trainings = predictions.getByModelAndType(model, Prediction.Type.TRAINING);
			Prediction training = (trainings.iterator()).next();

			Collection<Prediction> validations = predictions.getByModelAndType(model, Prediction.Type.VALIDATION);

			try {
				changed |= curate(model, training, validations);
			} catch(Exception e){
				logger.error("Model \'" + model.getId() + "\' not calculateable", e);
			}
		}

		if(changed){
			models.storeChanges();
			predictions.storeChanges();
		}

		return changed;
	}

	private boolean curate(Model model, Prediction training, Collection<Prediction> validations) throws Exception {
		Evaluator evaluator = org.dspace.content.QdbUtil.getEvaluator(model);

		if(evaluator != null){
			evaluator.init();

			try {

				if(!checkEvaluator(evaluator)){
					return false;
				}

				List<Descriptor> descriptors = evaluator.getDescriptors();

				LeverageCargo leverageCargo = model.getOrAddCargo(LeverageCargo.class);

				// XXX
				DecimalFormat format = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));

				if(true){
					Map<Compound, Double> leverages = leverageCargo.prepare(getCompounds(training), descriptors);

					LeverageValuesCargo leverageValuesCargo = training.getOrAddCargo(LeverageValuesCargo.class);
					leverageValuesCargo.storeDoubleMap(convertMap(leverages), format);
				}

				for(Prediction validation : validations){
					Map<Compound, Double> leverages = leverageCargo.predict(getCompounds(validation));

					LeverageValuesCargo leverageValuesCargo = validation.getOrAddCargo(LeverageValuesCargo.class);
					leverageValuesCargo.storeDoubleMap(convertMap(leverages), format);
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

		if(modelManager instanceof RegressionModelManager){
			return true;
		}

		return false;
	}

	static
	private List<Compound> getCompounds(Prediction prediction) throws IOException {
		List<Compound> compounds = new ArrayList<Compound>();

		Qdb qdb = prediction.getQdb();

		ValuesCargo valuesCargo = prediction.getCargo(ValuesCargo.class);

		Map<String, String> values = valuesCargo.loadStringMap();

		Collection<String> ids = values.keySet();
		for(String id : ids){
			Compound compound = qdb.getCompound(id);
			if(compound == null){
				throw new IllegalArgumentException("Compound \'" + id + "\' not found");
			}

			compounds.add(compound);
		}

		return compounds;
	}

	static
	public <V> Map<String, V> convertMap(Map<Compound, V> map){
		Map<String, V> result = new LinkedHashMap<String, V>();

		Collection<Map.Entry<Compound, V>> entries = map.entrySet();
		for(Map.Entry<Compound, V> entry : entries){
			result.put((entry.getKey()).getId(), entry.getValue());
		}

		return result;
	}

	private static final Logger logger = Logger.getLogger(QdbCalculateLeverageTask.class);
}