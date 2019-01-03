/*
 *  Copyright (c) 2019 University of Tartu
 */
package org.dspace.qsardb.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dspace.content.QdbModelUtil;
import org.dspace.content.QdbParameterUtil;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.evaluation.Evaluator;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;

public class DistanceCalculator {

	private String[] compoundIds;
	private String[] descriptorIds;
	private double[] minimas;
	private double[] maximas;
	private double[][] data;

	public DistanceCalculator(Model model) {
		try {
			init(model);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to  initilize DistanceCalculator for: "+model.getId(), ex);
		}
	}

	public ArrayList<Distance> calculateDistances(Map<String, String> params) throws Exception {
		double[] query = new double[descriptorIds.length];
		for (int j=0; j<descriptorIds.length; j++) {
			if (!params.containsKey(descriptorIds[j])) {
				throw new IllegalArgumentException("Missing value for: "+descriptorIds[j]);
			}
			String v = params.get(descriptorIds[j]);
			query[j] = norm(j, Double.parseDouble(v));
		}

		ArrayList<Distance> dists = new ArrayList<>(compoundIds.length);
		for (int i=0; i<compoundIds.length; i++) {
			double dist = 0.0;
			for (int j=0; j<descriptorIds.length; j++) {
				dist += Math.pow(query[j] - data[i][j], 2.0);
			}
			dists.add(new Distance(compoundIds[i], Math.sqrt(dist)));
		}
		Collections.sort(dists);

		return dists;
	}

	private double norm(int j, double v) {
		return (v - minimas[j]) / (maximas[j] - minimas[j]);
	}

	private void init(Model model) throws Exception {
		LinkedHashSet<String> cids = new LinkedHashSet<>();
		LinkedHashSet<String> trSet = new LinkedHashSet<>();
		for (Prediction pr: model.getQdb().getPredictionRegistry().getByModel(model)) {
			Set<String> keys = QdbParameterUtil.loadStringValues(pr).keySet();
			if (pr.getType().equals(Prediction.Type.TRAINING)) {
				trSet.addAll(keys);
			}
			cids.addAll(keys);
		}
		if (trSet.isEmpty()) {
			throw new IllegalArgumentException("No training set for: "+model.getId());
		}

		compoundIds = cids.toArray(new String[cids.size()]);
		initDescriptors(model);
		initDescriptorMatrix(model, descriptorIds, trSet);
	}

	private void initDescriptors(Model model) throws Exception {
		Evaluator eval = QdbModelUtil.getEvaluator(model);
		eval.init();
		try {
			List<Descriptor> descs = eval.getDescriptors();
			descriptorIds = new String[descs.size()];
			for (int j=0; j<descriptorIds.length; j++) {
				descriptorIds[j] = descs.get(j).getId();
			}
		} finally {
			eval.destroy();
		}

	}


	private void initDescriptorMatrix(Model model, String[] descs, LinkedHashSet<String> trSet) throws IOException {
		data = new double[compoundIds.length][descs.length];
		minimas = new double[descs.length];
		maximas = new double[descs.length];

		for (int j=0; j<descs.length; j++) {
			Descriptor d = model.getQdb().getDescriptor(descs[j]);
			ValuesCargo cargo = d.getCargo(ValuesCargo.class);
			Map<String, Double> dscMap = cargo.loadDoubleMap();

			minimas[j] = Double.MAX_VALUE;
			maximas[j] = Double.MIN_VALUE;
			for (int i=0; i<compoundIds.length; i++) {
				String key = compoundIds[i];
				double val = dscMap.getOrDefault(key, Double.NaN);
				data[i][j] = val;

				if (trSet.contains(key) && !Double.isNaN(val)) {
					minimas[j] = Math.min(data[i][j], minimas[j]);
					maximas[j] = Math.max(data[i][j], maximas[j]);
				}
			}

			for (int i=0; i<compoundIds.length; i++) {
				data[i][j] = norm(j, data[i][j]);
			}
		}
	}
}
