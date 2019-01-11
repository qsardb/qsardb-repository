/*
 *  Copyright (c) 2018 University of Tartu
 */
package org.dspace.qsardb.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.qsardb.cargo.map.DoubleFormat;

class ResponsesModel {

	private final boolean isRegression;
	private final double threshold;
	private final LinkedHashMap<String, Number> errors = new LinkedHashMap<>();

	public ResponsesModel(Map<String, String> predValues, Map<String, String> propValues, boolean isRegression) {
		this.isRegression = isRegression;
		SummaryStatistics stats = new SummaryStatistics();

		DoubleFormat fmt = new DoubleFormat();
		for (String cid: predValues.keySet()) {
			String es = propValues.get(cid);
			String cs = predValues.get(cid);
			if (es == null || cs == null) {
				continue;
			}

			if (isRegression) {
				Double ev = fmt.parse(es);
				Double cv = fmt.parse(cs);
				double err = Math.abs(ev-cv);
				stats.addValue(err);
				errors.put(cid, err);
			} else {
				errors.put(cid, es.equals(cs) ? 0 : 1);
			}
		}

		if (isRegression) {
			threshold = stats.getMean();
		} else {
			threshold = 0.3;
		}
	}

	public boolean estimate(List<Distance> neigbours) {
		SummaryStatistics stats = new SummaryStatistics();
		for (Distance n: neigbours) {
			stats.addValue(errors.get(n.getCompoundId()).doubleValue());
		}
		if (isRegression) {
			return stats.getMean() <= threshold;
		} else {
			return stats.getSum()/stats.getN() < threshold;
		}
	}
}
