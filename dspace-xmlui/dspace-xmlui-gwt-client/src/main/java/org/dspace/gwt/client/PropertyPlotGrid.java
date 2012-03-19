package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import org.dspace.gwt.rpc.*;

public class PropertyPlotGrid extends PlotGrid {

	public PropertyPlotGrid(QdbTable table){
		Resolver resolver = new Resolver(table);

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		Map<String, Object> propertyValues = property.getValues();
		ParameterUtil.Bounds propertyBounds = ParameterUtil.bounds(propertyValues);

		resize(2, 1);

		ParameterUtil.Bounds errorBounds = new ParameterUtil.Bounds();

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		for(PredictionColumn prediction : predictions){
			Map<String, Object> predictionValues = prediction.getValues();

			propertyBounds = ParameterUtil.bounds(propertyBounds, predictionValues);
			errorBounds = ParameterUtil.bounds(errorBounds, calculateErrorValues(predictionValues, propertyValues));
		}

		errorBounds = ParameterUtil.symmetricalBounds(errorBounds);

		ScatterPlot scatterPlot = new ScatterPlot(resolver, propertyBounds, propertyBounds);
		ScatterPlot errorPlot = new ScatterPlot(resolver, propertyBounds, errorBounds);

		for(PredictionColumn prediction : predictions){
			Map<String, Object> predictionValues = prediction.getValues();

			scatterPlot.addSeries(new PredictionSeries(prediction), property.getValues(), prediction.getValues());
			errorPlot.addSeries(new PredictionSeries(prediction), property.getValues(), calculateErrorValues(predictionValues, propertyValues));
		}

		setPlot(0, 0, scatterPlot);
		setPlot(1, 0, errorPlot);
	}

	static
	private Map<String, Object> calculateErrorValues(Map<String, Object> calcValues, Map<String, Object> expValues){
		Map<String, Object> result = new HashMap<String, Object>();

		Set<String> ids = calcValues.keySet();
		for(String id : ids){
			Object calcValue = calcValues.get(id);

			if(calcValue instanceof BigDecimal){
				Object expValue = expValues.get(id);

				if(expValue instanceof BigDecimal){
					BigDecimal error = ((BigDecimal)calcValue).subtract((BigDecimal)expValue);

					result.put(id, error);
				}
			}
		}

		return result;
	}
}