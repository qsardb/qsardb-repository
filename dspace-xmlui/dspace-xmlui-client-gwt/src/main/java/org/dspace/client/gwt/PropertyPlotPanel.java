package org.dspace.client.gwt;

import java.util.*;

import com.google.gwt.user.client.ui.*;

import org.dspace.rpc.gwt.*;

public class PropertyPlotPanel extends PlotPanel {

	public PropertyPlotPanel(QdbTable table, PropertyColumn property){
		Resolver resolver = new Resolver(table);

		Set<String> ids = new LinkedHashSet<String>();

		Map<String, Object> propertyValues = property.getValues();
		QdbPlot.Bounds propertyBounds = QdbPlot.bounds(propertyValues);

		ids.addAll(propertyValues.keySet());

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		for(PredictionColumn prediction : predictions){
			Map<String, Object> predictionValues = prediction.getValues();
			propertyBounds = QdbPlot.bounds(propertyBounds, predictionValues);

			ids.addAll(predictionValues.keySet());
		}

		ScatterPlot scatterPlot = new ScatterPlot(resolver);
		scatterPlot.addXAxisOptions(propertyBounds, property.getName() + " (exp.)");
		scatterPlot.addYAxisOptions(propertyBounds, property.getName() + " (calc.)");

		add(scatterPlot);

		// XXX
		add(new HTML("&nbsp;"));

		int size = Math.max((int)Math.sqrt(ids.size()), 10);

		HistogramPlot histogramPlot = new HistogramPlot(propertyBounds.getMin(), propertyBounds.getMax(), size);
		histogramPlot.addXAxisOptions(propertyBounds, property.getName() + " (exp.)");
		histogramPlot.addYAxisOptions("Frequency");

		add(histogramPlot);

		for(PredictionColumn prediction : predictions){
			Set<String> keys = (prediction.getValues()).keySet();

			Map<String, ?> predictionPropertyValues = ParameterUtil.subset(keys, propertyValues);

			scatterPlot.addSeries(new PredictionSeries(prediction), property.getValues(), prediction.getValues());
			histogramPlot.addSeries(new PredictionSeries(prediction), predictionPropertyValues);
		}
	}
}