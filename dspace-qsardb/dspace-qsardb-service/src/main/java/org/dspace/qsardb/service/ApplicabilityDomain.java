/*
 *  Copyright (c) 2018 University of Tartu
 */
package org.dspace.qsardb.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dspace.content.QdbParameterUtil;
import org.qsardb.evaluation.Evaluator;
import org.qsardb.evaluation.EvaluatorFactory;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;
import org.qsardb.model.PredictionRegistry;
import org.qsardb.statistics.RegressionStatistics;
import org.qsardb.statistics.Statistics;
import org.qsardb.statistics.StatisticsUtil;

/**
 * Applicability domain estimation for models in the QsarDB repository.
 */
public class ApplicabilityDomain {

	private final Set<String> trSet;
	private final DistanceCalculator dist;

	private final DescriptorRangesModel admDescriptorRanges;
	private final DistancesModel admDistances;
	private final ResponsesModel admResponses;

	public ApplicabilityDomain(Model m) {
		Map<String, String> propValues = QdbParameterUtil.loadStringValues(m.getProperty());

		Prediction training = null;
		PredictionRegistry predReg = m.getQdb().getPredictionRegistry();
		for (Prediction p: predReg.getByModelAndType(m, Prediction.Type.TRAINING)) {
			training = p;
		}

		Map<String, String> predValues = QdbParameterUtil.loadStringValues(training);

		List<Descriptor> descriptors = getDescriptors(m);
		Map<Descriptor, Map<String, Double>> descValues = new LinkedHashMap<>();
		for (Descriptor d: descriptors) {
			descValues.put(d, QdbParameterUtil.loadDoubleValues(d));
		}

		trSet = predValues.keySet();
		dist = new DistanceCalculator(m);

		admDescriptorRanges = new DescriptorRangesModel(trSet, descValues);
		admDistances = new DistancesModel(trSet, dist);

		Statistics modelStats = StatisticsUtil.evaluate(m, training);
		boolean isRegression = modelStats instanceof RegressionStatistics;
		admResponses = new ResponsesModel(predValues, propValues, isRegression);
	}

	/**
	 * Estimate applicability domain for a compound.
	 *
	 * @param descriptorValues  descriptor values {id: value, ...}
	 * @return true if the compound is in the applicability domain
	 */
	public Result estimate(Map<String, String> descriptorValues) {
		return estimate(descriptorValues, null);
	}

	/**
	 * Estimate applicability domain for a compound.
	 *
	 * @param descriptorValues  descriptor values {id: value, ...}
	 * @param excludedCompound  compound ID that will be excluded from the
	 * applicability domain evaluation. This may be null.
	 * @return true if the compound is in the applicability domain
	 */
	public Result estimate(Map<String, String> descriptorValues, String excludedCompound) {
		List<Distance> nn;
		if (excludedCompound == null) {
			nn = dist.calculateDistances(descriptorValues);
		} else {
			nn = dist.calculateDistances(excludedCompound);
		}

		int N = 5;
		ArrayList<Distance> nnList = new ArrayList<>(N);
		for (Distance n: nn) {
			if (trSet.contains(n.getCompoundId())) {
				nnList.add(n);
			}
			if (nnList.size() >= N) {
				break;
			}
		}

		int adScore = 0;
		adScore += admDescriptorRanges.estimate(descriptorValues) ? 1 : 0;
		adScore += admDistances.estimate(nnList) ? 1 : 0;
		adScore += admResponses.estimate(nnList) ? 1 : 0;

		Result result = new Result();
		result.withinAD = adScore >= 2;
		result.analogues = nn;
		return result;
	}

	private List<Descriptor> getDescriptors(Model model) {
		try {
			Evaluator eval = EvaluatorFactory.getInstance().getEvaluator(model);
			try {
				eval.init();
				return eval.getDescriptors();
			} finally {
				eval.destroy();
			}
		} catch (Exception ex) {
			return Collections.emptyList();
		}
	}

	public static class Result {
		private List<Distance> analogues;
		private boolean withinAD;

		public List<Distance> getAnalogues() {
			return analogues;
		}

		public boolean isWithinAD() {
			return withinAD;
		}
	}
}
