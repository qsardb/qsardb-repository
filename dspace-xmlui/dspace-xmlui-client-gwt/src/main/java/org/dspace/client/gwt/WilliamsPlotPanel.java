package org.dspace.client.gwt;

import java.math.*;
import java.util.*;

import org.dspace.rpc.gwt.*;

public class WilliamsPlotPanel extends PlotPanel {

	public WilliamsPlotPanel(QdbTable table, Class<? extends DistanceColumn> clazz){
		Resolver resolver = new Resolver(table);

		DistanceColumn distance = table.getColumn(clazz);

		Map<String, Object> distanceValues = distance.getValues();
		QdbPlot.Bounds distanceBounds = QdbPlot.bounds(distanceValues);

		QdbPlot.Bounds errorBounds = new QdbPlot.Bounds();

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		Map<String, BigDecimal> trainingErrors = null;

		for(PredictionColumn prediction : predictions){
			Map<String, BigDecimal> predictionErrors = prediction.getErrors();

			if((prediction.getType()).equals(PredictionColumn.Type.TRAINING)){
				trainingErrors = predictionErrors;
			}

			errorBounds = QdbPlot.bounds(errorBounds, predictionErrors);
		}

		errorBounds = QdbPlot.symmetricalBounds(errorBounds);

		BigDecimal criticalDistance = distance.getCriticalValue();

		// XXX
		distanceBounds.update(criticalDistance.multiply(new BigDecimal(1.10D), ParameterUtil.context));

		ScatterPlot scatterPlot = new ScatterPlot(resolver);
		scatterPlot.addXAxisOptions(distanceBounds, distance.getName());
		scatterPlot.addYAxisOptions(errorBounds, "Residual error");

		Number sigma = MathUtil.standardDeviation(trainingErrors.values());

		scatterPlot.addStDevMarkings(sigma);
		scatterPlot.addDistanceMarkings(criticalDistance);

		add(scatterPlot);

		for(PredictionColumn prediction : predictions){
			scatterPlot.addSeries(new PredictionSeries(prediction), distanceValues, prediction.getErrors());
		}
	}
}