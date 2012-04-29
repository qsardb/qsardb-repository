package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class DescriptorPlotPanel extends PlotPanel {

	public DescriptorPlotPanel(QdbTable table, PropertyColumn property, DescriptorColumn descriptor){
		Resolver resolver = new Resolver(table);

		Set<String> ids = new LinkedHashSet<String>();

		Map<String, Object> propertyValues = property.getValues();
		QdbPlot.Bounds propertyBounds = QdbPlot.bounds(propertyValues);

		ids.addAll(propertyValues.keySet());

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		for(PredictionColumn prediction : predictions){
			Map<String, Object> predictionValues = prediction.getValues();

			ids.addAll(predictionValues.keySet());
		}

		Map<String, Object> descriptorValues = descriptor.getValues();
		QdbPlot.Bounds descriptorBounds = QdbPlot.bounds(descriptorValues);

		ScatterPlot scatterPlot = new ScatterPlot(resolver);
		scatterPlot.addXAxisOptions(descriptorBounds, descriptor.getName());
		scatterPlot.addYAxisOptions(propertyBounds, property.getName() + " (exp.)");

		add(scatterPlot);

		// XXX
		add(new HTML("&nbsp;"));

		int size = Math.max((int)Math.sqrt(ids.size()), 10);

		HistogramPlot histogramPlot = new HistogramPlot(descriptorBounds.getMin(), descriptorBounds.getMax(), size);
		histogramPlot.addXAxisOptions(descriptorBounds, descriptor.getName());
		histogramPlot.addYAxisOptions("Frequency");

		add(histogramPlot);

		for(PredictionColumn prediction : predictions){
			Set<String> keys = (prediction.getValues()).keySet();

			Map<String, ?> predictionDescriptorValues = ParameterUtil.subset(keys, descriptorValues);

			scatterPlot.addSeries(new PredictionSeries(prediction), predictionDescriptorValues, propertyValues);
			histogramPlot.addSeries(new PredictionSeries(prediction), predictionDescriptorValues);
		}
	}
}