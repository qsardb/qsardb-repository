package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import ca.nanometrics.gflot.client.*;
import ca.nanometrics.gflot.client.options.*;

import org.dspace.gwt.rpc.*;

abstract
public class QdbPlot extends SimplePlot {

	public QdbPlot(){
		super(new QdbPlotModel(), new QdbPlotOptions());

		LegendOptions legendOptions = ensureLegendOptions();
		legendOptions.setShow(false);

		setPixelSize(320, 320);
	}

	public void changeSeriesVisibility(Map<PredictionColumn, Boolean> values){
		QdbPlotModel model = getModel();

		List<SeriesHandler> handlers = model.getHandlers();
		for(SeriesHandler handler : handlers){
			PredictionSeries series = (PredictionSeries)handler.getSeries();

			Boolean value = values.get(series.getPrediction());

			handler.setVisible(value != null ? value.booleanValue() : false);
		}

		redraw();
	}

	public void setXAxisBounds(ParameterUtil.Bounds bounds){
		ensureXAxesOptions().addAxisOptions(convertBounds(bounds));
	}

	public void setYAxisBounds(ParameterUtil.Bounds bounds){
		ensureYAxesOptions().addAxisOptions(convertBounds(bounds));
	}

	public GlobalSeriesOptions ensureGlobalSeriesOptions(){
		PlotOptions options = getPlotOptions();

		GlobalSeriesOptions globalSeriesOptions = options.getGlobalSeriesOptions();
		if(globalSeriesOptions == null){
			globalSeriesOptions = new GlobalSeriesOptions();

			options.setGlobalSeriesOptions(globalSeriesOptions);
		}

		return globalSeriesOptions;
	}

	public LineSeriesOptions ensureLineSeriesOptions(){
		GlobalSeriesOptions globalSeriesOptions = ensureGlobalSeriesOptions();

		LineSeriesOptions lineSeriesOptions = globalSeriesOptions.getLineSeriesOptions();
		if(lineSeriesOptions == null){
			lineSeriesOptions = new LineSeriesOptions();

			globalSeriesOptions.setLineSeriesOptions(lineSeriesOptions);
		}

		return lineSeriesOptions;
	}

	public GridOptions ensureGridOptions(){
		PlotOptions options = getPlotOptions();

		GridOptions gridOptions = options.getGridOptions();
		if(gridOptions == null){
			gridOptions = new GridOptions();

			options.setGridOptions(gridOptions);
		}

		return gridOptions;
	}

	public LegendOptions ensureLegendOptions(){
		PlotOptions options = getPlotOptions();

		LegendOptions legendOptions = options.getLegendOptions();
		if(legendOptions == null){
			legendOptions = new LegendOptions();

			options.setLegendOptions(legendOptions);
		}

		return legendOptions;
	}

	public AxesOptions ensureXAxesOptions(){
		PlotOptions options = getPlotOptions();

		AxesOptions axesOptions = options.getXAxesOptions();
		if(axesOptions == null){
			axesOptions = new AxesOptions();

			options.setXAxesOptions(axesOptions);
		}

		return axesOptions;
	}

	public AxesOptions ensureYAxesOptions(){
		PlotOptions options = getPlotOptions();

		AxesOptions axesOptions = options.getYAxesOptions();
		if(axesOptions == null){
			axesOptions = new AxesOptions();

			options.setYAxesOptions(axesOptions);
		}

		return axesOptions;
	}

	@Override
	public QdbPlotModel getModel(){
		return (QdbPlotModel)super.getModel();
	}

	@Override
	public QdbPlotOptions getPlotOptions(){
		return (QdbPlotOptions)super.getPlotOptions();
	}

	static
	public AxisOptions convertBounds(ParameterUtil.Bounds bounds){
		AxisOptions options = new AxisOptions();

		BigDecimal min = bounds.getMin();
		options.setMinimum(min.doubleValue());

		BigDecimal max = bounds.getMax();
		options.setMaximum(max.doubleValue());

		return options;
	}

	static
	protected class QdbPlotModel extends PlotModel {

		@Override
		public List<SeriesHandler> getHandlers(){
			return super.getHandlers();
		}
	}

	static
	protected class QdbPlotOptions extends PlotOptions {
	}
}