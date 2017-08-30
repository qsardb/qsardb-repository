/*
 * Copyright (c) 2014 University of Tartu
 */

package org.dspace.qsardb.client.gwt;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.dspace.qsardb.rpc.gwt.PredictionColumn;
import org.dspace.qsardb.rpc.gwt.PropertyColumn;
import org.dspace.qsardb.rpc.gwt.QdbTable;

class PropertyClassesPanel extends FlowPanel implements SeriesDisplayEventHandler {
	private final ArrayList<String> classes;
	private final PropertyColumn property;

	private int[][] confusion;
	private int total;
	private int[] actualCounts;
	private int[] predictedCounts;
	private double[] sensitivities;
	private double[] specificities;

	public PropertyClassesPanel(QdbTable table) {
		property = table.getColumn(PropertyColumn.class);

		classes = new ArrayList<String>();
		for (Object c: new HashSet<Object>(property.getValues().values())) {
			classes.add(c.toString());
		}
	}

	private void makeConfusionMatrix(PredictionColumn predCol) {
		Set<String> keys = predCol.getValues().keySet();
		Map<String, ?> predicted = ParameterUtil.subset(keys, predCol.getValues());
		Map<String, Object> actual = property.getValues();
		
		confusion = new int[classes.size()][classes.size()];
		for (String k: predicted.keySet()) {
			for (int i=0; i<classes.size(); ++i) {
				String ci = classes.get(i);
				for (int j=0; j<classes.size(); ++j) {
					String cj = classes.get(j);
					if (actual.get(k).equals(ci) && predicted.get(k).equals(cj)) {
						confusion[i][j]++;
					}
				}
			}
		}
		
		total = 0;
		actualCounts = new int[classes.size()];
		predictedCounts = new int[classes.size()];
		for (int i=0; i<classes.size(); ++i) {
			actualCounts[i] = 0;
			predictedCounts[i] = 0;
			for (int j=0; j<classes.size(); ++j) {
				actualCounts[i] += confusion[i][j];
				predictedCounts[i] += confusion[j][i];
			}
			total += actualCounts[i];
		}

		sensitivities = new double[classes.size()];
		specificities = new double[classes.size()];
		for (int i=0; i<classes.size(); ++i) {
			sensitivities[i] = confusion[i][i] / (double)actualCounts[i];
			specificities[i] = 0;
			for (int k=0; k<classes.size(); ++k) {
				if (k != i) {
					specificities[i] += predictedCounts[k] - confusion[i][k];
				}
			}
			specificities[i] /= total - actualCounts[i];
		}
	}

	@Override
	public void onSeriesVisibilityChanged(SeriesDisplayEvent event) {
		clear();

		for (PredictionColumn predCol: event.getValues(Boolean.TRUE)) {
			makeConfusionMatrix(predCol);

			add(new HTML(predCol.getId() + ": "+predCol.getName()));

			FlexTable table = new FlexTable();
			table.setStyleName("confusion-matrix");
			FlexTable.FlexCellFormatter formatter = (FlexTable.FlexCellFormatter) table.getCellFormatter();

			formatter.setColSpan(0, 0, 2);
			formatter.setRowSpan(0, 0, 2);
			formatter.setColSpan(0, 1, classes.size());
			table.setText(0, 1, "Predicted class");
			formatter.setColSpan(0, classes.size(), 3);
			table.setText(0, classes.size(), "Classification parameters");
			for (int i=0; i<classes.size(); ++i) {
				table.setText(1, i, classes.get(i));
			}
			table.setText(1, classes.size(), "Total");
			table.setText(1, classes.size()+1, "Sensitivity");
			table.setText(1, classes.size()+2, "Specificity");

			formatter.setRowSpan(2, 0, classes.size());
			table.setHTML(2, 0, "Actual<br/>class");

			int row = 2;
			for (int i=0; i<classes.size(); ++i, ++row) {
				int col = (row == 2) ? 1 : 0;
				table.setText(row, col++, classes.get(i));
				for (int j=0; j<classes.size(); ++j, ++col) {
					table.setText(row, col, String.valueOf(confusion[i][j]));
				}

				table.setText(row, col++, String.valueOf(actualCounts[i]));

				table.setText(row, col++, numFormat.format(sensitivities[i]));
				table.setText(row, col++, numFormat.format(specificities[i]));
			}

			table.setText(row, 1, "Total");
			for (int j=0; j<classes.size(); ++j) {
				table.setText(classes.size()+2, j+2, String.valueOf(predictedCounts[j]));
			}

			add(table);
		}
	}

	private static final NumberFormat numFormat = NumberFormat.getFormat("0.0000");
}
