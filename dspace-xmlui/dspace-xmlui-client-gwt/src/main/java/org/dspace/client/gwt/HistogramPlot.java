package org.dspace.client.gwt;

import java.math.*;
import java.util.*;

import com.googlecode.gflot.client.*;
import com.googlecode.gflot.client.options.*;

public class HistogramPlot extends QdbPlot {

	private BarList bars = null;

	private Map<PredictionSeries, List<HistogramDataPoint>> seriesPoints = new LinkedHashMap<PredictionSeries, List<HistogramDataPoint>>();


	public HistogramPlot(Number min, Number max, int size){
		this.bars = new BarList(min.doubleValue(), max.doubleValue(), size);

		final
		GlobalSeriesOptions globalSeriesOptions = ensureGlobalSeriesOptions();
		globalSeriesOptions.setStack(true);
		globalSeriesOptions.setShadowSize(0);

		LineSeriesOptions lineSeriesOptions = ensureLineSeriesOptions();
		lineSeriesOptions.setShow(false);

		Bar first = this.bars.first();
		Bar last = this.bars.last();

		double barWidth = (first.getWidth() + last.getWidth()) / 2;

		BarSeriesOptions barSeriesOptions = ensureBarSeriesOptions();
		barSeriesOptions.setAlignment(BarSeriesOptions.BarAlignment.CENTER);
		barSeriesOptions.setBarWidth(barWidth);
		barSeriesOptions.setShow(true);
	}

	@Override
	protected Map<PredictionSeries, List<? extends DataPoint>> getData(){
		return (Map)this.seriesPoints;
	}

	public void addSeries(PredictionSeries series, Map<String, ?> values){
		PlotModel model = getModel();

		SeriesHandler handler = model.addSeries(series);

		List<HistogramDataPoint> points = new ArrayList<HistogramDataPoint>();

		Map<Bar, List<String>> map = this.bars.map(values);

		Collection<Map.Entry<Bar, List<String>>> entries = map.entrySet();
		for(Map.Entry<Bar, List<String>> entry : entries){
			HistogramDataPoint point = HistogramDataPoint.create((entry.getKey()).getLocation(), entry.getValue());
			points.add(point);

			handler.add(point);
		}

		this.seriesPoints.put(series, points);
	}

	public int getMaxHeight(){
		int result = 0;

		for(int i = 0; i < this.bars.size(); i++){
			int height = 0;

			for(List<HistogramDataPoint> points : this.seriesPoints){
				HistogramDataPoint point = points.get(i);

				height += point.getY();
			}

			result = Math.max(height, result);
		}

		return result;
	}

	public BarSeriesOptions ensureBarSeriesOptions(){
		GlobalSeriesOptions globalSeriesOptions = ensureGlobalSeriesOptions();

		BarSeriesOptions barSeriesOptions = globalSeriesOptions.getBarSeriesOptions();
		if(barSeriesOptions == null){
			barSeriesOptions = BarSeriesOptions.create();

			globalSeriesOptions.setBarsSeriesOptions(barSeriesOptions);
		}

		return barSeriesOptions;
	}

	public void addYAxisOptions(String label){
		super.addYAxisOptions(null, label);
	}

	static
	private class BarList extends ArrayList<Bar> {

		private BarList(double min, double max, int size){
			double width = (max - min) / size;

			double lower = min;
			double upper = lower + width;

			add(new Bar(lower, upper));

			for(int i = 1; i < size - 1; i++){
				lower = upper;
				upper = lower + width;

				add(new Bar(lower, upper));
			}

			lower = upper;
			upper = max;

			add(new Bar(lower, upper));
		}

		public Bar getBar(double value){
			int index = (int)((value - getMin()) / (getMax() - getMin()) * size());

			return get(Math.max(Math.min(index, size() - 1), 0));
		}

		public <K> Map<Bar, List<K>> map(Map<K, ?> values){
			Map<Bar, List<K>> result = new HashMap<Bar, List<K>>();

			List<Bar> bars = this;
			for(Bar bar : bars){
				result.put(bar, new ArrayList<K>());
			}

			Set<K> keys = values.keySet();
			for(K key : keys){
				Object value = values.get(key);

				if(value instanceof BigDecimal){
					Bar bar = getBar(((BigDecimal)value).doubleValue());

					List<K> keyList = result.get(bar);

					keyList.add(key);
				}
			}

			return result;
		}

		public Bar first(){
			return get(0);
		}

		public Bar last(){
			return get(size() - 1);
		}

		public double getMin(){
			return first().getMin();
		}

		public double getMax(){
			return last().getMax();
		}
	}

	static
	private class Bar {

		private double min = Double.MIN_VALUE;

		private double max = Double.MAX_VALUE;


		private Bar(double min, double max){
			setMin(min);
			setMax(max);
		}

		public double getWidth(){
			return getMax() - getMin();
		}

		public double getLocation(){
			return getMin() + getWidth() / 2;
		}

		public double getMin(){
			return this.min;
		}

		private void setMin(double min){
			this.min = min;
		}

		public double getMax(){
			return this.max;
		}

		private void setMax(double max){
			this.max = max;
		}
	}
}