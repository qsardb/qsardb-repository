/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.qsardb.rpc.gwt;

import java.util.LinkedHashMap;
import java.util.Map;

public class PredictorRequest {

	private String structure;

	private Map<String, String> parameters;

	public PredictorRequest() {
	}
	
	public PredictorRequest(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public PredictorRequest(String structure) {
		this.structure = structure;
	}

	public String getStructure() {
		return structure;
	}

	public void setStructure(String structure) {
		this.structure = structure;
	}

	public Map<String, String> getParameters() {
		if (parameters == null) {
			parameters = new LinkedHashMap<String, String>();
		}
		return parameters;
	}
	
	public void setParameters(Map<String, String> params) {
		parameters = params;
	}

	@Override
	public String toString() {
		return "PredictorRequest:"+structure+":"+parameters;
	}
}
