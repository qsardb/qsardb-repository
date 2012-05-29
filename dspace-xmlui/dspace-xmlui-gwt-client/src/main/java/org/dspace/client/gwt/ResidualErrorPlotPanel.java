package org.dspace.client.gwt;

import java.math.*;
import java.util.*;

import ca.nanometrics.gflot.client.options.*;

import org.dspace.rpc.gwt.*;

public class ResidualErrorPlotPanel extends PlotPanel {

	public ResidualErrorPlotPanel(QdbTable table, PropertyColumn property){
		Resolver resolver = new Resolver(table);

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

		add(scatterPlot);

		for(PredictionColumn prediction : predictions){
			scatterPlot.addSeries(new PredictionSeries(prediction), property.getValues(), prediction.getErrors());
		}

		Number sigma = MathUtil.standardDeviation(trainingErrors.values());

		Markings markings = new Markings();
		markings.addMarkings(createStDevMarkings(sigma, Double.valueOf(2), QdbPlot.COLOR_TWO_SIGMA));
		markings.addMarkings(createStDevMarkings(sigma, Double.valueOf(3), QdbPlot.COLOR_THREE_SIGMA));

		GridOptions gridOptions = scatterPlot.ensureGridOptions();
		gridOptions.setMarkings(markings);
	}

	static
	private Marking[] createStDevMarkings(Number sigma, Number multiplier, String color){
		double value = (sigma.doubleValue() * multiplier.doubleValue());

		return new Marking[]{
			createLineMarking(Double.valueOf(-1 * value), color),
			createLineMarking(Double.valueOf(value), color)
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