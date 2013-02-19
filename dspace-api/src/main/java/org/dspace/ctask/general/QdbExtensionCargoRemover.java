package org.dspace.ctask.general;

import java.io.*;
import java.util.*;

import org.qsardb.model.*;

public class QdbExtensionCargoRemover extends QdbTask {

	@Override
	public boolean accept(Qdb qdb){
		ModelRegistry models = qdb.getModelRegistry();
		PredictionRegistry predictions = qdb.getPredictionRegistry();

		for(Model model : models){

			if(accept(model)){
				return true;
			}
		}

		for(Prediction prediction : predictions){

			if(accept(prediction)){
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
			changed |= curate(model);
		}

		for(Prediction prediction : predictions){
			changed |= curate(prediction);
		}

		if(changed){
			models.storeChanges();
			predictions.storeChanges();
		}

		return changed;
	}

	private boolean accept(Container<?, ?> container){
		Set<String> ids = new LinkedHashSet<String>(container.getCargos());

		for(String id : ids){

			if(accept(id)){
				return true;
			}
		}

		return false;
	}

	private boolean curate(Container<?, ?> container){
		Set<String> ids = new LinkedHashSet<String>(container.getCargos());

		boolean changed = false;

		for(String id : ids){

			if(accept(id)){
				container.removeCargo(id);

				changed |= true;
			}
		}

		return changed;
	}

	private boolean accept(String id){
		return id != null && id.startsWith("org.qsardb.cargo.");
	}
}