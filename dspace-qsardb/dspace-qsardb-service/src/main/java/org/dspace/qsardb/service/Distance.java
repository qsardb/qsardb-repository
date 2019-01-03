/*
 *  Copyright (c) 2019 University of Tartu
 */
package org.dspace.qsardb.service;

public class Distance implements Comparable<Distance> {
	
	final private String compId;
	final private double distance;

	public Distance(String compoundId, double distance) {
		this.compId = compoundId;
		this.distance = distance;
	}

	public String getCompoundId() {
		return compId;
	}

	public double getDistance() {
		return distance;
	}

	@Override
	public int compareTo(Distance o) {
		return Double.compare(distance, o.distance);
	}

	@Override
	public String toString() {
		return String.format("%s: %.4f", compId, distance);
	}

}
