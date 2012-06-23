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
		super(new QdbPlotModel(), new PlotOptions());

		LegendOptions legendOptions = ensureLegendOptions();
		legendOptions.setShow(false);

		setSize(PLOT_SIZE);
	}

	public void setSize(int size){
		setSize(size, false);
	}

	public void setSize(int size, boolean refresh){
		setWidth(size + (refresh ? getBorderWidth() : 0));
		setHeight(size + (refresh ? getBorderHeight() : 0));
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
					int size = Math.min(Window.getClientWidth(), Window.getClientHeight()) - 50;

					setSize(size, true);

					this.popup.center();
				} else

				{
					// Resize before attach
					setSize(PLOT_SIZE, true);

					parent.setWidget(widget);

					this.popup.hide();
				}
			}
		};

		addDomHandler(clickHandler, DoubleClickEvent.getType());
	}

	public void addXAxisOptions(Bounds bounds){
		addXAxisOptions(bounds, null);
	}

	public void addXAxisOptions(Bounds bounds, String label){
		AxisOptions axisOptions = convertBounds(bounds);
		axisOptions.setLabelHeight(PLOT_BORDER_SIZE);

		setHeight(getHeight() + PLOT_BORDER_SIZE);

		if(isValid(label)){
			axisOptions.setLabel(label);

			setHeight(getHeight() + PLOT_LABEL_SIZE);
		}

		ensureXAxesOptions().addAxisOptions(axisOptions);
	}

	private int getBorderWidth(){
		return getBorderSize(ensureXAxesOptions());
	}

	public void addYAxisOptions(Bounds bounds){
		addYAxisOptions(bounds, null);
	}

	public void addYAxisOptions(Bounds bounds, String label){
		AxisOptions axisOptions = convertBounds(bounds);
		axisOptions.setLabelWidth(PLOT_BORDER_SIZE);

		setWidth(getWidth() + PLOT_BORDER_SIZE);

		if(isValid(label)){
			axisOptions.setLabel(label);

			setWidth(getWidth() + PLOT_LABEL_SIZE);
		}

		ensureYAxesOptions().addAxisOptions(axisOptions);
	}

	private int getBorderHeight(){
		return getBorderSize(ensureYAxesOptions());
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

	static
	private int getBorderSize(AxesOptions axesOptions){
		int size = 0;

		AbstractAxisOptions<?>[] axisOptions = axesOptions.getAxesOptions();
		for(AbstractAxisOptions<?> axisOption : axisOptions){
			size += PLOT_BORDER_SIZE;

			String label = axisOption.getLabel();

			if(isValid(label)){
				size += PLOT_LABEL_SIZE;
			}
		}

		return size;
	}

	static
	private boolean isValid(String label){
		return (label != null && !("").equals(label));
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

	public static final int PLOT_SIZE = 360;

	public static final int PLOT_BORDER_SIZE = 20;
	public static final int PLOT_LABEL_SIZE = 16;

	public static final String COLOR_TWO_SIGMA = "#ffff00";
	public static final String COLOR_THREE_SIGMA = "#ff8080";

	public static final String COLOR_CRITICAL_DISTANCE = "#ff8080";
}