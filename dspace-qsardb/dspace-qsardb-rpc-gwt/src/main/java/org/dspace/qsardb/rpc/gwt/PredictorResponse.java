/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.qsardb.rpc.gwt;

import java.util.LinkedHashMap;
import java.util.Map;

public class PredictorResponse {

	private String result;
	private Map<String, String> parameters;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
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
}
