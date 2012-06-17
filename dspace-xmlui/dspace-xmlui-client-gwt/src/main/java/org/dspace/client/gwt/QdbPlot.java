package org.dspace.client.gwt;

import java.math.*;
import java.util.*;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import ca.nanometrics.gflot.client.*;
import ca.nanometrics.gflot.client.options.*;

import org.dspace.rpc.gwt.*;

abstract
public class QdbPlot extends SimplePlot {

	public QdbPlot(){
		super(new QdbPlotModel(), new QdbPlotOptions());

		LegendOptions legendOptions = ensureLegendOptions();
		legendOptions.setShow(false);

		setSize(SIZE + 20);
	}

	public void changeSeriesVisibility(SeriesDisplayEvent event){
		QdbPlotModel model = getModel();

		Set<PredictionColumn> visiblePredictions = event.getValues(Boolean.TRUE);

		List<SeriesHandler> handlers = model.getHandlers();
		for(SeriesHandler handler : handlers){
			PredictionSeries series = (PredictionSeries)handler.getSeries();

			handler.setVisible(visiblePredictions.contains(series.getPrediction()));
		}

		if(isAttached()){
			redraw();
		}
	}

	public void enableDetach(final HasOneWidget parent){
		DoubleClickHandler clickHandler = new DoubleClickHandler(){

			private PopupPanel popup = new PopupPanel(false, true);

			{
				this.popup.setGlassEnabled(true);
			}

			@Override
			public void onDoubleClick(DoubleClickEvent event){
				QdbPlot widget = QdbPlot.this;

				boolean attached = (widget.getParent()).equals(parent);

				if(attached){
					this.popup.setWidget(widget);

					this.popup.center();

					// XXX
					int size = Math.min(Window.getClientWidth(), Window.getClientHeight()) - 40;

					widget.setSize(size + 20);

					this.popup.center();
				} else

				{
					// Resize before attach
					widget.setSize(SIZE + 20);

					parent.setWidget(widget);

					this.popup.hide();
				}
			}
		};

		addDomHandler(clickHandler, DoubleClickEvent.getType());
	}

	public void setSize(int size){
		setWidth(size);
		setHeight(size);
	}

	public void addXAxisOptions(Bounds bounds){
		addXAxisOptions(bounds, null);
	}

	public void addXAxisOptions(Bounds bounds, String label){
		AxisOptions axisOptions = convertBounds(bounds);
		axisOptions.setLabelHeight(20);

		if(label != null){
			axisOptions.setLabel(label);

			// XXX
			setHeight(getHeight() + 16);
		}

		ensureXAxesOptions().addAxisOptions(axisOptions);
	}

	public void addYAxisOptions(Bounds bounds){
		addYAxisOptions(bounds, null);
	}

	public void addYAxisOptions(Bounds bounds, String label){
		AxisOptions axisOptions = convertBounds(bounds);
		axisOptions.setLabelWidth(20);

		if(label != null){
			axisOptions.setLabel(label);

			// XXX
			setWidth(getWidth() + 16);
		}

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

		if(bounds != null){
			options.setMinimum((bounds.getMin()).doubleValue());
			options.setMaximum((bounds.getMax()).doubleValue());
		}

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

		public BigDecimal getRange(){
			return (getMax()).subtract(getMin());
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

	public static final int SIZE = 360;

	public static final String COLOR_TWO_SIGMA = "#ffff00";
	public static final String COLOR_THREE_SIGMA = "#ff8080";

	public static final String COLOR_CRITICAL_DISTANCE = "#ff8080";
}