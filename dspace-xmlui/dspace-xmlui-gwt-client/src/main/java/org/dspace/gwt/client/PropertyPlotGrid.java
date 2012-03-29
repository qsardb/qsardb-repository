package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import ca.nanometrics.gflot.client.options.*;

import org.dspace.gwt.rpc.*;

public class PropertyPlotGrid extends PlotGrid {

	public PropertyPlotGrid(QdbTable table){
		Resolver resolver = new Resolver(table);

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		Map<String, Object> propertyValues = property.getValues();
		QdbPlot.Bounds propertyBounds = QdbPlot.bounds(propertyValues);

		resize(2, 1);

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		QdbPlot.Bounds errorBounds = new QdbPlot.Bounds();

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
		scatterPlot.setXAxisBounds(propertyBounds);
		scatterPlot.setYAxisBounds(propertyBounds);

		ScatterPlot errorScatterPlot = new ScatterPlot(resolver);
		errorScatterPlot.setXAxisBounds(propertyBounds);
		errorScatterPlot.setYAxisBounds(errorBounds);

		for(PredictionColumn prediction : predictions){
			scatterPlot.addSeries(new PredictionSeries(prediction), property.getValues(), prediction.getValues());
			errorScatterPlot.addSeries(new PredictionSeries(prediction), property.getValues(), prediction.getErrors());
		}

		Number stDev = MathUtil.standardDeviation(trainingErrors.values());

		Markings markings = new Markings();
		markings.addMarkings(createStDevMarkings(stDev, Double.valueOf(2), "#ffff00"));
		markings.addMarkings(createStDevMarkings(stDev, Double.valueOf(3), "#ff8080"));

		GridOptions gridOptions = errorScatterPlot.ensureGridOptions();
		gridOptions.setMarkings(markings);

		setPlot(0, 0, scatterPlot);
		setPlot(1, 0, errorScatterPlot);
	}

	static
	private Marking[] createStDevMarkings(Number stDev, Number multiplier, String color){
		return new Marking[]{
			createLineMarking(Double.valueOf(-1 * stDev.doubleValue() * multiplier.doubleValue()), color),
			createLineMarking(Double.valueOf(stDev.doubleValue() * multiplier.doubleValue()), color)
		};
	}

	static
	private Marking createLineMarking(Number value, String color){
		Marking result = new Marking();
		result.setY(new Range(value.doubleValue(), value.doubleValue()));
		result.setColor(color);
		result.setLineWidth(1);

		return result;
	}
}