package org.dspace.gwt.client;

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

	public void setXAxisBounds(Bounds bounds){
		ensureXAxesOptions().addAxisOptions(convertBounds(bounds));
	}

	public void setYAxisBounds(Bounds bounds){
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
	public AxisOptions convertBounds(Bounds bounds){
		AxisOptions options = new AxisOptions();
		options.setMinimum((bounds.getMin()).doubleValue());
		options.setMaximum((bounds.getMax()).doubleValue());

		return options;
	}

	static
	public Bounds bounds(Map<?, ?> map){
		return bounds(null, map);
	}

	static
	public Bounds bounds(Bounds bounds, Map<?, ?> map){
		Bounds result = new Bounds(bounds);

		Collection<?> values = map.values();
		for(Object value : values){

			if(value instanceof Number){
				result.update((Number)value);
			}
		}

		return result;
	}

	static
	public Bounds symmetricalBounds(Bounds bounds){
		Bounds result = new Bounds();

		double max = Math.max(Math.abs((bounds.getMin()).doubleValue()), Math.abs((bounds.getMax()).doubleValue()));

		result.setMin(Double.valueOf(-1 * max));
		result.setMax(Double.valueOf(max));

		return result;
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

	static
	public class Bounds {

		private Number min = null;

		private Number max = null;


		public Bounds(){
		}

		public Bounds(Bounds bounds){

			if(bounds != null){
				setMin(bounds.getMin());
				setMax(bounds.getMax());
			}
		}

		public void update(Number value){

			if(value == null){
				return;
			} // End if

			if(this.min == null || compare(this.min, value) > 0){
				this.min = value;
			} // End if

			if(this.max == null || compare(this.max, value) < 0){
				this.max = value;
			}
		}

		public Number getMin(){
			return this.min;
		}

		public void setMin(Number min){
			this.min = min;
		}

		public Number getMax(){
			return this.max;
		}

		public void setMax(Number max){
			this.max = max;
		}

		static
		private double compare(Number left, Number right){
			return (left.doubleValue() - right.doubleValue());
		}
	}
}