/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.qsardb.rpc.gwt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PredictorResponse {

	//predicted value
	private String result;

	private Map<String, String> predictionValues;

	//units for predicted value
	private String resultUnits;

	private Map<String, String> descriptorValues;

	private Map<String, String> implementations;

	private List<Analogue> analogues;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Map<String, String> getPredictionValues() {
		if (predictionValues == null) {
			this.predictionValues = new LinkedHashMap<>();
		}
		return predictionValues;
	}

	public void setPredictionValues(Map<String, String> predictionValues) {
		this.predictionValues = predictionValues;
	}

	public String getResultUnits() {
		return resultUnits;
	}

	public void setResultUnits(String resultUnits) {
		this.resultUnits = resultUnits;
	}

	public Map<String, String> getDescriptorValues() {
		if (descriptorValues == null) {
			descriptorValues = new LinkedHashMap<String, String>();
		}
		return descriptorValues;
	}

	public void setDescriptorValues(Map<String, String> parameters) {
		this.descriptorValues = parameters;
	}

	public Map<String, String> getImplementations() {
		if (implementations == null) {
			implementations = new LinkedHashMap<String, String>();
		}
		return implementations;
	}

	public void setImplementations(Map<String, String> implementations) {
		this.implementations = implementations;
	}

	public List<Analogue> getAnalogues() {
		if (analogues == null) {
			analogues = new ArrayList<>();
		}
		return analogues;
	}

	public void setAnalogues(List<Analogue> analogues) {
		this.analogues = analogues;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("PredictorResponse");
		sb.append(":").append(result);
		sb.append(":").append(resultUnits);
		sb.append(":").append(getDescriptorValues());
		sb.append(":").append(getImplementations());
		sb.append(":").append(getAnalogues());

		return sb.toString();
	}
}
