package org.dspace.client.gwt;

import java.math.*;
import java.util.*;

import org.dspace.rpc.gwt.*;

public class InsubriaPlotPanel extends PlotPanel {

	public InsubriaPlotPanel(QdbTable table){
		Resolver resolver = new Resolver(table);

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		LeverageColumn leverage = table.getColumn(LeverageColumn.class);

		Map<String, Object> leverageValues = leverage.getValues();
		QdbPlot.Bounds leverageBounds = QdbPlot.bounds(leverageValues);

		QdbPlot.Bounds predictionBounds = new QdbPlot.Bounds();

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		for(PredictionColumn prediction : predictions){
			Map<String, Object> predictionValues = prediction.getValues();

			predictionBounds = QdbPlot.bounds(predictionBounds, predictionValues);
		}

		BigDecimal criticalLeverage = leverage.getCriticalValue();

		// XXX
		leverageBounds.update(criticalLeverage.multiply(new BigDecimal(1.10D), ParameterUtil.context));

		ScatterPlot scatterPlot = new ScatterPlot(resolver);
		scatterPlot.addXAxisOptions(leverageBounds, "Leverage");
		scatterPlot.addYAxisOptions(predictionBounds, property.getName() + " (calc.)");

		scatterPlot.addDistanceMarkings(criticalLeverage);

		add(scatterPlot);

		for(PredictionColumn prediction : predictions){
			scatterPlot.addSeries(new PredictionSeries(prediction), leverageValues, prediction.getValues());
		}
	}
}