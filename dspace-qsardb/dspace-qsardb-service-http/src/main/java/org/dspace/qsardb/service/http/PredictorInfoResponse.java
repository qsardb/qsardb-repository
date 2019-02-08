/*
 *  Copyright (c) 2019 University of Tartu
 */
package org.dspace.qsardb.service.http;

import java.util.Map;
import java.util.LinkedHashMap;

public class PredictorInfoResponse {

	private Map<String, String> descriptorNames = new LinkedHashMap<>();

	private Map<String, String> descriptorApplications = new LinkedHashMap<>();

	private boolean acceptSMILES = false;

	public Map<String, String> getDescriptorNames() {
		return descriptorNames;
	}

	public void setDescriptorNames(Map<String, String> descriptorNames) {
		this.descriptorNames = descriptorNames;
	}

	public Map<String, String> getDescriptorApplications() {
		return descriptorApplications;
	}

	public void setDescriptorApplications(Map<String, String> descriptorApplications) {
		this.descriptorApplications = descriptorApplications;
	}

	public boolean getAcceptSMILES() {
		return acceptSMILES;
	}

	public void setAcceptSMILES(boolean acceptSMILES) {
		this.acceptSMILES = acceptSMILES;
	}
}
