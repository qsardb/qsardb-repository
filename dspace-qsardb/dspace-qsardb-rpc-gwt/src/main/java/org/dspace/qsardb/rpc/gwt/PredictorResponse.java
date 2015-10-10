/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.qsardb.rpc.gwt;

import java.util.LinkedHashMap;
import java.util.Map;

public class PredictorResponse {

	//predicted value
	private String result;

	//units for predicted value
	private String resultUnits;

	private Map<String, String> parameters;

	private Map<String, String> implementations;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResultUnits() {
		return resultUnits;
	}

	public void setResultUnits(String resultUnits) {
		this.resultUnits = resultUnits;
	}

	public Map<String, String> getParameters() {
		if (parameters == null) {
			parameters = new LinkedHashMap<String, String>();
		}
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nResponse -- contains:\n");

		if (parameters != null) {
			sb.append("Values: ").append(parameters.keySet().size()).append("\n");
			for(String key : parameters.keySet()) {
				sb.append(key).append(" : ").append(parameters.get(key)).append("\n");
			}
			sb.append("\n");
		}

		if (implementations != null) {
			sb.append("Implementations: ").append(implementations.keySet().size()).append("\n");
			for(String key : implementations.keySet()) {
				sb.append(key).append(" : ").append(implementations.get(key)).append("\n");
			}
			sb.append("\n");
		}

		return sb.toString();
	}
}
