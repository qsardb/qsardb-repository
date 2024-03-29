/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.qsardb.rpc.gwt;

import java.util.LinkedHashMap;
import java.util.Map;

public class PredictorRequest {

	private String structure;

	private Map<String, String> descriptorValues;

	private Integer limitAnalogues;

	public PredictorRequest() {
	}
	
	public PredictorRequest(Map<String, String> parameters) {
		this.descriptorValues = parameters;
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

	public Map<String, String> getDescriptorValues() {
		if (descriptorValues == null) {
			descriptorValues = new LinkedHashMap<>();
		}
		return descriptorValues;
	}
	
	public void setDescriptorValues(Map<String, String> params) {
		descriptorValues = params;
	}

	public Integer getLimitAnalogues() {
		if (limitAnalogues == null) {
			return 5;
		} else {
			return limitAnalogues;
		}
	}

	public void setLimitAnalogues(Integer limit) {
		this.limitAnalogues = limit;
	}

	@Override
	public String toString() {
		return "PredictorRequest:"+structure+":"+descriptorValues;
	}
}
