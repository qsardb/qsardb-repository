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

		setPixelSize(300 + 20, 300 + 20);
	}

	public void changeSeriesVisibility(SeriesDisplayEvent event){
		QdbPlotModel model = getModel();

		Set<PredictionColumn> visiblePredictions = event.getValues(Boolean.TRUE);

		List<SeriesHandler> handlers = model.getHandlers();
		for(SeriesHandler handler : handlers){
			PredictionSeries series = (PredictionSeries)handler.getSeries();

			handler.setVisible(visiblePredictions.contains(series.getPrediction()));
		}

		redraw();
	}

	public void setXAxisBounds(Bounds bounds){
		setXAxisBounds(bounds, 20);
	}

	public void setXAxisBounds(Bounds bounds, int height){
		AxisOptions axisOptions = convertBounds(bounds);
		axisOptions.setLabelHeight(height);

		ensureXAxesOptions().addAxisOptions(axisOptions);
	}

	public void setYAxisBounds(Bounds bounds){
		setYAxisBounds(bounds, 20);
	}

	public void setYAxisBounds(Bounds bounds, int width){
		AxisOptions axisOptions = convertBounds(bounds);
		axisOptions.setLabelWidth(width);

		ensureYAxesOptions().addAxisOptions(axisOptions);
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

			if(value instanceof BigDecimal){
				result.update((BigDecimal)value);
			}
		}

		return result;
	}

	static
	public Bounds symmetricalBounds(Bounds bounds){
		Bounds result = new Bounds();

		BigDecimal max = (bounds.getMin().abs()).max(bounds.getMax().abs());

		result.setMin(MINUS_ONE.multiply(max));
		result.setMax(max);

		return result;
	}

	private static final BigDecimal MINUS_ONE = new BigDecimal(-1);

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

		private BigDecimal min = null;

		private BigDecimal max = null;


		public Bounds(){
		}

		public Bounds(Bounds bounds){

			if(bounds != null){
				setMin(bounds.getMin());
				setMax(bounds.getMax());
			}
		}

		public int scale(){
			return Math.max(getMin().scale(), getMax().scale());
		}

		public MathContext getMathContext(){
			return new MathContext(scale(), RoundingMode.HALF_UP);
		}

		public void update(BigDecimal value){

			if(value == null){
				return;
			} // End if

			if(this.min == null || (this.min).compareTo(value) > 0){
				this.min = value;
			} // End if

			if(this.max == null || (this.max).compareTo(value) < 0){
				this.max = value;
			}
		}

		public BigDecimal getMin(){
			return this.min;
		}

		public void setMin(BigDecimal min){
			this.min = min;
		}

		public BigDecimal getMax(){
			return this.max;
		}

		public void setMax(BigDecimal max){
			this.max = max;
		}
	}

	public static final String COLOR_TWO_SIGMA = "#ffff00";
	public static final String COLOR_THREE_SIGMA = "#ff8080";
}