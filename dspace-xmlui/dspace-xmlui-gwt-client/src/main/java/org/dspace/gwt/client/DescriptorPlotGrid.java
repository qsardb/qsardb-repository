package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import org.dspace.gwt.rpc.*;

public class DescriptorPlotGrid extends PlotGrid {

	public DescriptorPlotGrid(QdbTable table){
		Resolver resolver = new Resolver(table);

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		Map<String, Object> propertyValues = property.getValues();
		QdbPlot.Bounds propertyBounds = QdbPlot.bounds(propertyValues);

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);

		resize(2, descriptors.size());

		Set<String> ids = new LinkedHashSet<String>(propertyValues.keySet());

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		for(PredictionColumn prediction : predictions){
			Map<String, Object> predictionValues = prediction.getValues();

			ids.addAll(predictionValues.keySet());
		}

		int size = Math.max((int)Math.sqrt(ids.size()), 10);

		int height = 0;

		for(int i = 0; i < descriptors.size(); i++){
			DescriptorColumn descriptor = descriptors.get(i);

			Map<String, Object> descriptorValues = descriptor.getValues();
			QdbPlot.Bounds descriptorBounds = QdbPlot.bounds(descriptorValues);

			ScatterPlot scatterPlot = new ScatterPlot(resolver);
			scatterPlot.addXAxisOptions(descriptorBounds, descriptor.getName());
			scatterPlot.addYAxisOptions(propertyBounds, property.getName() + " (exp.)");

			HistogramPlot histogramPlot = new HistogramPlot(descriptorBounds.getMin(), descriptorBounds.getMax(), size);
			histogramPlot.addXAxisOptions(descriptorBounds, descriptor.getName());

			for(PredictionColumn prediction : predictions){
				Set<String> keys = (prediction.getValues()).keySet();

				Map<String, ?> predictionDescriptorValues = ParameterUtil.subset(keys, descriptorValues);

				scatterPlot.addSeries(new PredictionSeries(prediction), predictionDescriptorValues, propertyValues);
				histogramPlot.addSeries(new PredictionSeries(prediction), predictionDescriptorValues);
			}

			setPlot(0, i, scatterPlot);
			setPlot(1, i, histogramPlot);

			height = Math.max(histogramPlot.getMaxHeight(), height);
		}

		QdbPlot.Bounds yBounds = new QdbPlot.Bounds();
		yBounds.setMin(BigDecimal.ZERO);
		yBounds.setMax(new BigDecimal(height));

		for(int i = 0; i < descriptors.size(); i++){
			HistogramPlot histogramPlot = (HistogramPlot)getPlot(1, i);

			histogramPlot.addYAxisOptions(yBounds, "Frequency");
		}
	}
}