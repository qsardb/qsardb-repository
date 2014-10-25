package org.dspace.qsardb.client.gwt;

import java.util.*;

import org.dspace.qsardb.rpc.gwt.*;

import com.google.gwt.core.client.*;

import com.googlecode.gflot.client.*;

public class PredictionSeries extends Series {

	protected PredictionSeries(){
	}

	final
	public PredictionColumn getPrediction(){
		return PredictionSeries.predictions.get(this);
	}

	final
	public void setPrediction(PredictionColumn prediction){
		PredictionSeries.predictions.put(this, prediction);
	}

	static
	public PredictionSeries create(PredictionColumn prediction){
		PredictionSeries result = (JavaScriptObject.createObject()).cast();
		result.setLabel(prediction.getName());
		// XXX result.setAutoGeneratedColor((prediction.getType()).ordinal());
		result.setPrediction(prediction);

		return result;
	}

	// Subclasses of JavaScriptObject cannot have instance fields
	private static final Map<PredictionSeries, PredictionColumn> predictions = new HashMap<PredictionSeries, PredictionColumn>();
}