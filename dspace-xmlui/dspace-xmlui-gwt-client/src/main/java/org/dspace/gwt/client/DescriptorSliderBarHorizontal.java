package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import com.google.gwt.core.client.*;
import com.google.gwt.resources.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import com.kiouri.sliderbar.client.view.*;

public class DescriptorSliderBarHorizontal extends SliderBarHorizontal {

	private QdbPlot.Bounds bounds = null;

	private List<Marking> markings = new ArrayList<Marking>();


	public DescriptorSliderBarHorizontal(int maxValue, QdbPlot.Bounds bounds){
		setMaxValue(maxValue);

		setLessWidget(new Image(images.less()));
		setScaleWidget(new Image(images.scale().getSafeUri()), 15);
		setMoreWidget(new Image(images.more()));

		setDragWidget(new Image(images.drag()));

		setWidth((20 + maxValue + 20) + "px");

		setBounds(bounds);
	}

	@Override
	protected void placeWidgets(ArrayList<Widget> widgets){
		super.placeWidgets(widgets);

		for(Marking marking : this.markings){
			super.absPanel.add(marking, super.startPosition + marking.getValue(), 0);
		}
	}

	public void addMarkings(Double mean, Double sigma){
		addMeanMarking(mean);

		addStDevMarkings(mean, sigma, Double.valueOf(2), QdbPlot.COLOR_TWO_SIGMA);
		addStDevMarkings(mean, sigma, Double.valueOf(3), QdbPlot.COLOR_THREE_SIGMA);
	}

	private void addMeanMarking(Double mean){
		addMarking(mean, "#000000", "Avg");
	}

	private void addStDevMarkings(Double mean, Double sigma, Double multiplier, String color){
		double value = (sigma.doubleValue() * multiplier.doubleValue());

		addMarking(Double.valueOf(mean.doubleValue() - value), color, ("-" + multiplier.intValue() + "\u03C3"));
		addMarking(Double.valueOf(mean.doubleValue() + value), color, ("+" + multiplier.intValue() + "\u03C3"));
	}

	private void addMarking(Number value, String color, String title){
		MathContext context = getMathContext();

		addMarking(new BigDecimal(value.toString(), context), color, title);
	}

	private void addMarking(BigDecimal value, String color, String title){

		try {
			value = validate(value, false);
		} catch(IllegalArgumentException iae){
			return;
		}

		Marking marking = new Marking(color, toSliderValue(value));
		marking.setTitle(title + ": " + value.toString());

		this.markings.add(marking);
	}

	public MathContext getMathContext(){
		return this.bounds.getMathContext();
	}

	public BigDecimal getUserValue(){
		return fromSliderValue(getValue());
	}

	private BigDecimal fromSliderValue(int value){
		MathContext context = getMathContext();

		BigDecimal min = this.bounds.getMin();
		BigDecimal max = this.bounds.getMax();

		BigDecimal sliderMaxValue = new BigDecimal(getMaxValue());
		BigDecimal sliderValue = new BigDecimal(value);

		BigDecimal userValue = (min).add((max.subtract(min)).multiply(sliderValue).divide(sliderMaxValue, context));

		return userValue.setScale(context.getPrecision(), context.getRoundingMode());
	}

	public void setUserValue(BigDecimal value){
		value = validate(value, true);

		setValue(toSliderValue(value));
	}

	private int toSliderValue(BigDecimal userValue){
		MathContext context = getMathContext();

		BigDecimal min = this.bounds.getMin();
		BigDecimal max = this.bounds.getMax();

		BigDecimal sliderMaxValue = new BigDecimal(getMaxValue());
		BigDecimal sliderValue = ((userValue).subtract(min)).divide(max.subtract(min), context).multiply(sliderMaxValue);

		return sliderValue.intValue();
	}

	private BigDecimal validate(BigDecimal value, boolean replace){
		QdbPlot.Bounds bounds = getBounds();

		BigDecimal min = bounds.getMin();
		if(min != null && (min).compareTo(value) > 0){

			if(replace){
				return min;
			}

			throw new IllegalArgumentException();
		}

		BigDecimal max = bounds.getMax();
		if(max != null && (max).compareTo(value) < 0){

			if(replace){
				return max;
			}

			throw new IllegalArgumentException();
		}

		return value;
	}

	public QdbPlot.Bounds getBounds(){
		return this.bounds;
	}

	private void setBounds(QdbPlot.Bounds bounds){
		this.bounds = bounds;
	}

	private class Marking extends AbsolutePanel {

		private int value = 0;


		public Marking(String color, int value){
			setPixelSize(1, 15);

			Element element = getElement();
			DOM.setStyleAttribute(element, "backgroundColor", color);

			setValue(value);
		}

		public int getValue(){
			return this.value;
		}

		private void setValue(int value){
			this.value = value;
		}
	}

	interface Images extends ClientBundle {

		@Source (
			value = "slider_drag.png"
		)
		ImageResource drag();

		@Source (
			value = "slider_less.png"
		)
		ImageResource less();

		@Source (
			value = "slider_more.png"
		)
		ImageResource more();

		@Source (
			value = "slider_scale.png"
		)
		DataResource scale();
	}

	private static final Images images = GWT.create(Images.class);
}