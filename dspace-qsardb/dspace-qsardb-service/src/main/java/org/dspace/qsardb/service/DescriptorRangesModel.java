/*
 *  Copyright (c) 2019 University of Tartu
 */
package org.dspace.qsardb.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.qsardb.model.Descriptor;

class DescriptorRangesModel {

	private final LinkedHashMap<String, SummaryStatistics> ranges = new LinkedHashMap<>();
	private final double limit = 3.0;

	public DescriptorRangesModel(Set<String> trSet, Map<Descriptor, Map<String, Double>> descValues) {
		for (Map.Entry<Descriptor, Map<String, Double>> e: descValues.entrySet()) {
			SummaryStatistics stats = new SummaryStatistics();
			for (String cid: trSet) {
				stats.addValue(e.getValue().get(cid));
			}
			ranges.put(e.getKey().getId(), stats);
		}
	}

	public Map<String, Double> zScores(Map<String, String> params) {
		LinkedHashMap<String, Double> r = new LinkedHashMap<>(ranges.size());
		for (Map.Entry<String, SummaryStatistics> e: ranges.entrySet()) {
			SummaryStatistics stats = e.getValue();
			double dv = Double.parseDouble(params.get(e.getKey()));

			if (stats.getStandardDeviation() != 0) {
				double z = (dv - stats.getMean()) / stats.getStandardDeviation();
				r.put(e.getKey(), z);
			}
		}

		return r;
	}

	boolean estimate(Map<String, Double> zScores) {
		int countAbove = 0;
		for (Double z: zScores.values()) {
			if (Math.abs(z) > limit) {
				countAbove++;
			}
		}

		return countAbove == 0;
	}
}
