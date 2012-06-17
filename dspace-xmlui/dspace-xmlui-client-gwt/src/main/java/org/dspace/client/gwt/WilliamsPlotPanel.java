package org.dspace.client.gwt;

import java.math.*;
import java.util.*;

import org.dspace.rpc.gwt.*;

public class WilliamsPlotPanel extends PlotPanel {

	public WilliamsPlotPanel(QdbTable table){
		Resolver resolver = new Resolver(table);

		LeverageColumn leverage = table.getColumn(LeverageColumn.class);

		Map<String, Object> leverageValues = leverage.getValues();
		QdbPlot.Bounds leverageBounds = QdbPlot.bounds(leverageValues);

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

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);

		MathContext context = new MathContext(6);

		// XXX
		BigDecimal criticalLeverage = (new BigDecimal(3)).multiply(new BigDecimal(1 + descriptors.size()), context).divide(new BigDecimal((trainingErrors.values()).size()), context);

		// XXX
		leverageBounds.update(criticalLeverage.multiply(new BigDecimal(1.10D), context));

		ScatterPlot scatterPlot = new ScatterPlot(resolver);
		scatterPlot.addXAxisOptions(leverageBounds, "Leverage");
		scatterPlot.addYAxisOptions(errorBounds, "Residual error");

		Number sigma = MathUtil.standardDeviation(trainingErrors.values());

		scatterPlot.addStDevMarkings(sigma);
		scatterPlot.addDistanceMarkings(criticalLeverage);

		add(scatterPlot);

		for(PredictionColumn prediction : predictions){
			scatterPlot.addSeries(new PredictionSeries(prediction), leverageValues, prediction.getErrors());
		}
	}
}