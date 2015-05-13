package org.dspace.qsardb.client.gwt;

import java.math.*;
import java.util.*;

import org.dspace.qsardb.rpc.gwt.*;

public class GramaticaPlotPanel extends PlotPanel {

	public GramaticaPlotPanel(QdbTable table, Class<? extends DistanceColumn> clazz){
		Resolver resolver = new Resolver(table);

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		DistanceColumn distance = table.getColumn(clazz);

		Map<String, Object> distanceValues = distance.getValues();
		QdbPlot.Bounds distanceBounds = QdbPlot.bounds(distanceValues);

		QdbPlot.Bounds predictionBounds = new QdbPlot.Bounds();

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		for(PredictionColumn prediction : predictions){
			Map<String, Object> predictionValues = prediction.getValues();

			predictionBounds = QdbPlot.bounds(predictionBounds, predictionValues);
		}

		BigDecimal criticalDistance = distance.getCriticalValue();

		// XXX
		distanceBounds.update(criticalDistance.multiply(BigDecimal.valueOf(1.10D), ParameterUtil.context));

		ScatterPlot scatterPlot = new ScatterPlot(resolver);
		scatterPlot.addXAxisOptions(distanceBounds, distance.getName());
		scatterPlot.addYAxisOptions(predictionBounds, "Calculated property");

		scatterPlot.addDistanceMarkings(criticalDistance);

		add(scatterPlot);

		for(PredictionColumn prediction : predictions){
			scatterPlot.addSeries(PredictionSeries.create(prediction), distanceValues, prediction.getValues());
		}
	}
}