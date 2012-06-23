package org.dspace.client.gwt;

import java.math.*;
import java.util.*;

import ca.nanometrics.gflot.client.*;
import ca.nanometrics.gflot.client.event.*;
import ca.nanometrics.gflot.client.jsni.*;
import ca.nanometrics.gflot.client.options.*;

public class ScatterPlot extends QdbPlot {

	private List<List<ScatterDataPoint>> seriesPoints = new ArrayList<List<ScatterDataPoint>>();


	public ScatterPlot(final Resolver resolver){
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

					this.tooltip.schedule(point.getId(), (item.getPageX()).intValue(), (item.getPageY()).intValue());
				} else

				{
					this.tooltip.cancel();
				}
			}
		};
		addHoverListener(hoverListener, false);
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

	public void addStDevMarkings(Number sigma){
		Markings markings = ensureMarkings();
		markings.addMarkings(createStDevMarkings(sigma, Double.valueOf(2), QdbPlot.COLOR_TWO_SIGMA));
		markings.addMarkings(createStDevMarkings(sigma, Double.valueOf(3), QdbPlot.COLOR_THREE_SIGMA));
	}

	public void addDistanceMarkings(Number distance){
		Markings markings = ensureMarkings();
		markings.addMarking(createXMarking(distance, QdbPlot.COLOR_CRITICAL_DISTANCE));
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

	public Markings ensureMarkings(){
		GridOptions gridOptions = ensureGridOptions();

		Markings markings = gridOptions.getMarkings();
		if(markings == null){
			markings = new Markings();

			gridOptions.setMarkings(markings);
		}

		return markings;
	}

	private ScatterDataPoint getDataPoint(PlotItem item){
		return (this.seriesPoints).get((item.getSeriesIndex()).intValue()).get((item.getDataIndex()).intValue());
	}

	static
	private Marking[] createStDevMarkings(Number sigma, Number multiplier, String color){
		double value = (sigma.doubleValue() * multiplier.doubleValue());

		return new Marking[]{
			createYMarking(Double.valueOf(-1 * value), color),
			createYMarking(Double.valueOf(value), color)
		};
	}

	static
	private Marking createXMarking(Number value, String color){
		Marking result = createMarking(color);
		result.setX(new Range(value.doubleValue(), value.doubleValue()));

		return result;
	}

	static
	private Marking createYMarking(Number value, String color){
		Marking result = createMarking(color);
		result.setY(new Range(value.doubleValue(), value.doubleValue()));

		return result;
	}

	static
	private Marking createMarking(String color){
		Marking result = new Marking();
		result.setColor(color);
		result.setLineWidth(1);

		return result;
	}
}