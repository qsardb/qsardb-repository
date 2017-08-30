package org.dspace.qsardb.client.gwt;

import java.util.*;

import com.google.gwt.user.client.ui.*;
import java.util.logging.Logger;

import org.dspace.qsardb.rpc.gwt.*;

public class PropertyPlotPanel extends PlotPanel {

	public PropertyPlotPanel(QdbTable table){
		Resolver resolver = new Resolver(table);

		Set<String> ids = new LinkedHashSet<String>();

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		Map<String, Object> propertyValues = property.getValues();
		QdbPlot.Bounds propertyBounds = QdbPlot.bounds(propertyValues);

		ids.addAll(propertyValues.keySet());

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		for(PredictionColumn prediction : predictions){
			Map<String, ?> predictionValues = ParameterUtil.subset(
					propertyValues.keySet(), prediction.getValues());
			propertyBounds = QdbPlot.bounds(propertyBounds, predictionValues);

			ids.addAll(predictionValues.keySet());
		}

		ScatterPlot scatterPlot = new ScatterPlot(resolver);
		scatterPlot.addXAxisOptions(propertyBounds, "Experimental");
		scatterPlot.addYAxisOptions(propertyBounds, "Calculated");

		add(scatterPlot);

		// XXX
		add(new HTML("&nbsp;"));

		int size = Math.max((int)Math.sqrt(ids.size()), 10);

		HistogramPlot histogramPlot = new HistogramPlot(propertyBounds.getMin(), propertyBounds.getMax(), size);
		histogramPlot.addXAxisOptions(propertyBounds, "Experimental");
		histogramPlot.addYAxisOptions("Frequency");

		add(histogramPlot);

		for(PredictionColumn prediction : predictions){
			Set<String> keys = (prediction.getValues()).keySet();

			Map<String, ?> predictionPropertyValues = ParameterUtil.subset(keys, propertyValues);

			scatterPlot.addSeries(PredictionSeries.create(prediction), property.getValues(), prediction.getValues());
			histogramPlot.addSeries(PredictionSeries.create(prediction), predictionPropertyValues);
		}
	}
}