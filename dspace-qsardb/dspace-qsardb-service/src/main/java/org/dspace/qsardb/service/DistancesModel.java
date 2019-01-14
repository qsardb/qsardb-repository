/*
 *  Copyright (c) 2018 University of Tartu
 */
package org.dspace.qsardb.service;

import java.util.List;
import java.util.Set;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

class DistancesModel {

	final double threshold;
	final int neighboursLimit = 1;

	public DistancesModel(Set<String> trSet, DistanceCalculator dist) {
		SummaryStatistics trStats = new SummaryStatistics();

		int idx = Math.min(neighboursLimit, trSet.size() - 1) - 1;
		for (String cid: trSet) {
			List<Distance> nn = dist.calculateDistances(cid);
			trStats.addValue(nn.get(idx).getDistance());
		}

		this.threshold = trStats.getMean() + 2*trStats.getStandardDeviation();
	}

	public boolean estimate(List<Distance> nearestNeigbours) {
		int idx = Math.min(neighboursLimit, nearestNeigbours.size()) - 1;
		return nearestNeigbours.get(idx).getDistance() <= threshold;
	}
}
