package org.dspace.client.gwt;

import java.math.*;
import java.util.*;

import org.dspace.rpc.gwt.*;

public class ResidualErrorPlotPanel extends PlotPanel {

	public ResidualErrorPlotPanel(QdbTable table){
		Resolver resolver = new Resolver(table);

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		Map<String, Object> propertyValues = property.getValues();
		QdbPlot.Bounds propertyBounds = QdbPlot.bounds(propertyValues);

		QdbPlot.Bounds errorBounds = new QdbPlot.Bounds();

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		Map<String, BigDecimal> trainingErrors = null;

		for(PredictionColumn prediction : predictions){
			Map<String, Object> predictionValues = prediction.getValues();
			Map<String, BigDecimal> predictionErrors = prediction.getErrors();

			if((prediction.getType()).equals(PredictionColumn.Type.TRAINING)){
				trainingErrors = predictionErrors;
			}

			propertyBounds = QdbPlot.bounds(propertyBounds, predictionValues);
			errorBounds = QdbPlot.bounds(errorBounds, predictionErrors);
		}

		errorBounds = QdbPlot.symmetricalBounds(errorBounds);

		ScatterPlot scatterPlot = new ScatterPlot(resolver);
		scatterPlot.addXAxisOptions(propertyBounds, property.getName() + " (exp.)");
		scatterPlot.addYAxisOptions(errorBounds, "Residual error");

		Number sigma = MathUtil.standardDeviation(trainingErrors.values());

		scatterPlot.addStDevMarkings(sigma);

		add(scatterPlot);

		for(PredictionColumn prediction : predictions){
			scatterPlot.addSeries(PredictionSeries.create(prediction), property.getValues(), prediction.getErrors());
		}
	}
}