package org.dspace.ctask.general;

import java.io.*;
import java.text.*;
import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.cargo.matrix.*;
import org.qsardb.model.*;

import org.apache.log4j.*;

abstract
public class QdbModelTask extends QdbTask {

	abstract
	public boolean accept(Model model);

	abstract
	public boolean curate(Model model, Prediction training, Collection<Prediction> validations) throws Exception;

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

	static
	public <C extends MatrixCargo> boolean hasDistance(Model model, Class<? extends C> clazz){

		if(model.hasCargo(clazz)){
			C matrixCargo = model.getCargo(clazz);

			try {
				matrixCargo.loadObject();

				return true;
			} catch(Exception e){
				return false;
			}
		}

		return false;
	}

	static
	public List<Compound> getCompounds(Prediction prediction) throws IOException {
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

	public static final DecimalFormat format = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));

	private static final Logger logger = Logger.getLogger(QdbModelTask.class);
}