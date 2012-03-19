package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import ca.nanometrics.gflot.client.*;
import ca.nanometrics.gflot.client.event.*;
import ca.nanometrics.gflot.client.jsni.*;
import ca.nanometrics.gflot.client.options.*;

public class ScatterPlot extends QdbPlot {

	private List<List<ScatterDataPoint>> seriesPoints = new ArrayList<List<ScatterDataPoint>>();


	public ScatterPlot(final Resolver resolver, Bounds xBounds, Bounds yBounds){
		GlobalSeriesOptions globalSeriesOptions = ensureGlobalSeriesOptions();
		globalSeriesOptions.setShadowSize(0);

		LineSeriesOptions lineSeriesOptions = ensureLineSeriesOptions();
		lineSeriesOptions.setShow(false);

		PointsSeriesOptions pointSeriesOptions = ensurePointsSeriesOptions();
		pointSeriesOptions.setRadius(3);
		pointSeriesOptions.setShow(true);

		GridOptions gridOptions = ensureGridOptions();
		gridOptions.setHoverable(true);

		PlotHoverListener hoverListener = new PlotHoverListener(){

			ResolverTooltip tooltip = new ResolverTooltip(resolver);


			@Override
			public void onPlotHover(Plot plot, PlotPosition position, PlotItem item){

				if(item != null){
					ScatterDataPoint point = getDataPoint(item);

					this.tooltip.schedule(point.getId(), item.getPageX() + 5, item.getPageY() + 5);
				} else

				{
					this.tooltip.cancel();
				}
			}
		};
		addHoverListener(hoverListener, false);

		setXAxisBounds(xBounds);
		setYAxisBounds(yBounds);
	}

	public void addSeries(Series series, Map<String, ?> xValues, Map<String, ?> yValues){
		PlotModel model = getModel();

		SeriesHandler handler = model.addSeries(series);

		Set<String> ids = new LinkedHashSet<String>(xValues.keySet());
		ids.retainAll(yValues.keySet());

		List<ScatterDataPoint> points = new ArrayList<ScatterDataPoint>();

		for(String id : ids){
			Object x = xValues.get(id);
			Object y = yValues.get(id);

			if(x instanceof BigDecimal && y instanceof BigDecimal){
				ScatterDataPoint point = new ScatterDataPoint(((BigDecimal)x).doubleValue(), ((BigDecimal)y).doubleValue(), id);
				points.add(point);

				handler.add(point);
			}
		}

		this.seriesPoints.add(points);
	}

	public PointsSeriesOptions ensurePointsSeriesOptions(){
		GlobalSeriesOptions globalSeriesOptions = ensureGlobalSeriesOptions();

		PointsSeriesOptions pointSeriesOptions = globalSeriesOptions.getPointsSeriesOptions();
		if(pointSeriesOptions == null){
			pointSeriesOptions = new PointsSeriesOptions();

			globalSeriesOptions.setPointsOptions(pointSeriesOptions);
		}

		return pointSeriesOptions;
	}

	private ScatterDataPoint getDataPoint(PlotItem item){
		return (this.seriesPoints).get(item.getSeriesIndex()).get(item.getDataIndex());
	}
}