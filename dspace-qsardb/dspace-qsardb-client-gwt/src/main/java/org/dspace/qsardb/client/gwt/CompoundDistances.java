package org.dspace.qsardb.client.gwt;

import java.util.*;
import org.dspace.qsardb.rpc.gwt.DescriptorColumn;
import org.dspace.qsardb.rpc.gwt.ModelTable;
import org.dspace.qsardb.rpc.gwt.PredictionColumn;

public class CompoundDistances {
	private final ModelTable table;
	private final Collection<String> compIds;
	private final List<DescriptorColumn> descColumns;
	private final ArrayList<Distance> distances;
	private double[] minimas;
	private double[] maximas;

	public CompoundDistances(ModelTable table, Collection<String> compIds) {
		this.table = table;
		this.compIds = compIds;
		this.descColumns = table.getAllColumns(DescriptorColumn.class);
		this.distances = new ArrayList<Distance>(compIds.size());
	}

	public void calculate(Map<String, String> descriptorValues) {
		if (distances.isEmpty()) {
			init();
		}

		double[] query = new double[descColumns.size()];
		for (int j=0; j<descColumns.size(); j++) {
			String descId = descColumns.get(j).getId();
			query[j] = Double.parseDouble(descriptorValues.get(descId));
		}
		normalize(query);

		for (Distance d: distances) {
			double dist = 0.0;
			for (int j=0; j<descColumns.size(); j++) {
				dist += Math.pow(query[j] - d.descs[j], 2.0);
			}
			d.distance = Math.sqrt(dist);
		}

		Collections.sort(distances);
	}

	int size() {
		return distances.size();
	}

	String getId(int i) {
		return distances.get(i).compId;
	}

	double getDistance(int i) {
		return distances.get(i).distance;
	}

	private void init() {
		PredictionColumn training = PredictionColumn.filter(
				table.getAllColumns(PredictionColumn.class),
				PredictionColumn.Type.TRAINING).get(0);

		// init descriptor ranges for normalization from the training set
		minimas = new double[descColumns.size()];
		maximas = new double[descColumns.size()];
		Set<String> trainingCompIds = training.getValues().keySet();
		for (int i=0; i<descColumns.size(); i++) {
			Map<String, Object> descValues = descColumns.get(i).getValues();
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			for (String compId: trainingCompIds) {
				Object o = descValues.get(compId);
				if(o instanceof Number){
					double v = ((Number)o).doubleValue();
					min= Math.min(v, min);
					max= Math.max(v, max);
				}
			}

			minimas[i] = min;
			maximas[i] = max;
		}

		// normalize descriptors for distance calculation
		for (String compId: compIds) {
			double darr[] = new double[descColumns.size()];
			for (int j=0; j<descColumns.size(); j++) {
				Object o = descColumns.get(j).getValue(compId);
				if (o instanceof Number) {
					darr[j] = ((Number)o).doubleValue();
				}
			}
			normalize(darr);
			distances.add(new Distance(compId, darr));
		}
	}

	private void normalize(double[] descs) {
		for (int j=0; j<descs.length; j++) {
			descs[j] = (descs[j] - minimas[j]) / (maximas[j] - minimas[j]);
		}
	}

	private static class Distance implements Comparable<Distance> {
		final String compId;
		final double[] descs;
		double distance;

		private Distance(String compId, double[] descs) {
			this.compId = compId;
			this.descs = descs;
		}

		@Override
		public int compareTo(Distance o) {
			return Double.compare(distance, o.distance);
		}
	}
}