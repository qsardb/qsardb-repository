/*
 *  Copyright (c) 2018 University of Tartu
 */
package org.dspace.qsardb.service;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import org.qsardb.model.Descriptor;

class DescriptorRangesModel {

	private final LinkedHashMap<String, DescriptiveStatistics> ranges = new LinkedHashMap<>();

	public DescriptorRangesModel(Set<String> trSet, Map<Descriptor, Map<String, Double>> descValues) {
		for (Map.Entry<Descriptor, Map<String, Double>> e: descValues.entrySet()) {
			DescriptiveStatistics stats = new DescriptiveStatistics();
			for (String cid: trSet) {
				stats.addValue(e.getValue().get(cid));
			}
			ranges.put(e.getKey().getId(), stats);
		}
	}

	public boolean estimate(Map<String, String> params) {
		int score = 0;
		for (Map.Entry<String, DescriptiveStatistics> e: ranges.entrySet()) {
			DescriptiveStatistics stats = e.getValue();
			double dv = Double.parseDouble(params.get(e.getKey()));

			double limit = 3.0;
			if (Math.abs(dv - stats.getMean()) > limit*stats.getStandardDeviation()) {
				++score;
			}
		}

		return score == 0;
	}
}
