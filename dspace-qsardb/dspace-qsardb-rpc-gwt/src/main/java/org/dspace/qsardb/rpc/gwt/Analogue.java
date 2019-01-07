/*
 *  Copyright (c) 2019 University of Tartu
 */
package org.dspace.qsardb.rpc.gwt;

import java.util.LinkedHashMap;
import java.util.Map;

public class Analogue {

	// Compound identifier as string.
	private String compoundId;

	// Euclidean distance calculated from descriptor values.
	private double distance;

	// Compound name
	private String name;

	// CAS Registry Number
	private String cas;

	// SMILES string. Might be absent for few compounds in the repository.
	private String smiles;

	// Prediction values in the dataset
	private Map<String, String> predictionValues = new LinkedHashMap<>();

	// Property values in the dataset
	private Map<String, String> propertyValues = new LinkedHashMap<>();

	public Analogue() {
	}

	public Analogue(String compoundId) {
		this.compoundId = compoundId;
	}

	public String getCompoundId() {
		return compoundId;
	}

	public void setCompoundId(String compoundId) {
		this.compoundId = compoundId;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCas() {
		return cas;
	}

	public void setCas(String cas) {
		this.cas = cas;
	}

	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public Map<String, String> getPredictionValues() {
		return predictionValues;
	}

	public void setPredictionValues(Map<String, String> predictionValues) {
		this.predictionValues = predictionValues;
	}

	public Map<String, String> getPropertyValues() {
		return propertyValues;
	}

	public void setPropertyValues(Map<String, String> propertyValues) {
		this.propertyValues = propertyValues;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Analogue");
		sb.append(":id=").append(compoundId);
		sb.append(":dist=").append(distance);
		sb.append(":name=").append(name);
		sb.append(":props=").append(propertyValues);
		sb.append(":preds=").append(predictionValues);
		return sb.toString();
	}

}
