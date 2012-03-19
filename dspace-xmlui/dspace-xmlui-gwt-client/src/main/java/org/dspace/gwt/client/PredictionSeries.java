package org.dspace.gwt.client;

import org.dspace.gwt.rpc.*;

import ca.nanometrics.gflot.client.*;

public class PredictionSeries extends Series {

	private PredictionColumn prediction = null;


	public PredictionSeries(PredictionColumn prediction){
		super(prediction.getName());

		setPrediction(prediction);
	}

	public PredictionColumn getPrediction(){
		return this.prediction;
	}

	private void setPrediction(PredictionColumn prediction){
		this.prediction = prediction;
	}
}