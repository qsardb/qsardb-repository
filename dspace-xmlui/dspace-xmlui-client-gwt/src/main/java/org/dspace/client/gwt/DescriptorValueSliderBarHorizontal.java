package org.dspace.client.gwt;

import java.math.*;
import java.util.*;

import com.google.gwt.core.client.*;
import com.google.gwt.resources.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import com.kiouri.sliderbar.client.view.*;

public class DescriptorValueSliderBarHorizontal extends SliderBarHorizontal {

	private QdbPlot.Bounds bounds = null;

	private List<Marking> markings = new ArrayList<Marking>();


	public DescriptorValueSliderBarHorizontal(QdbPlot.Bounds bounds){
		this(bounds, QdbPlot.PLOT_SIZE);
	}

	public DescriptorValueSliderBarHorizontal(QdbPlot.Bounds bounds, int maxValue){
		setBounds(bounds);

		Image less = new Image(images.less());
		(less.getElement()).getStyle().setZIndex(100);
		setLessWidget(less);

		setScaleWidget(new Image(images.scale().getSafeUri()), 15);

		Image more = new Image(images.more());
		(more.getElement()).getStyle().setZIndex(100);
		setMoreWidget(more);

		Image drag = new Image(images.drag());
		setDragWidget(drag);

		setMaxValue(maxValue);

		setWidth(String.valueOf(20 + QdbPlot.PLOT_SIZE + 20) + "px");
	}

	@Override
	protected void placeWidgets(ArrayList<Widget> widgets){
		super.placeWidgets(widgets);

		for(Marking marking : this.markings){
			super.absPanel.add(marking, super.startPosition + marking.getValue() - marking.getOffsetWidth() / 2, 0);
		}
	}

	public void addMarkings(BigDecimal mean, BigDecimal sigma){
		addMeanMarking(mean);

		addStDevMarkings(mean, sigma, new BigDecimal(2), QdbPlot.COLOR_TWO_SIGMA);
		addStDevMarkings(mean, sigma, new BigDecimal(3), QdbPlot.COLOR_THREE_SIGMA);
	}

	private void addMeanMarking(BigDecimal mean){
		addMarking(mean, "#000000", "Avg");
	}

	private void addStDevMarkings(BigDecimal mean, BigDecimal sigma, BigDecimal multiplier, String color){
		addMarking(mean.add(sigma.multiply(multiplier)), color, ("+" + multiplier.intValue() + SIGMA));
		addMarking(mean.subtract(sigma.multiply(multiplier)), color, ("-" + multiplier.intValue() + SIGMA));
	}

	private void addMarking(Number value, String color, String title){

		try {
			value = validate(value, false);
		} catch(IllegalArgumentException iae){
			return;
		}

		Marking marking = new Marking(color, toSliderValue(value));
		marking.setTitle(title + ": " + value.toString());

		this.markings.add(marking);
	}

	@Override
	public void setValue(int value){
		super.setValue(value);
	}

	public Number getUserValue(){
		return fromSliderValue(getValue());
	}

	private Double fromSliderValue(int value){
		BigDecimal min = this.bounds.getMin();
		BigDecimal max = this.bounds.getMax();

		return Double.valueOf(min.doubleValue() + (max.doubleValue() - min.doubleValue()) * value / getMaxValue());
	}

	public void setUserValue(Number value){
		value = validate(value, true);

		setValue(toSliderValue(value));
	}

	private int toSliderValue(Number value){
		BigDecimal min = this.bounds.getMin();
		BigDecimal max = this.bounds.getMax();

		return (int)((value.doubleValue() - min.doubleValue()) / (max.doubleValue() - min.doubleValue()) * getMaxValue());
	}

	private Number validate(Number value, boolean replace){
		QdbPlot.Bounds bounds = getBounds();

		BigDecimal min = bounds.getMin();
		if(min != null && value.doubleValue() < min.doubleValue()){

			if(replace){
				return min;
			}

			throw new IllegalArgumentException();
		}

		BigDecimal max = bounds.getMax();
		if(max != null && value.doubleValue() > max.doubleValue()){

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
			setPixelSize(2, 20);

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

	private static final String SIGMA = "\u03C3";

	interface Images extends ClientBundle {

		@Source (
			value = "images/sliderDrag.png"
		)
		ImageResource drag();

		@Source (
			value = "images/sliderLess.png"
		)
		ImageResource less();

		@Source (
			value = "images/sliderMore.png"
		)
		ImageResource more();

		@Source (
			value = "images/sliderScale.png"
		)
		DataResource scale();
	}

	private static final Images images = GWT.create(Images.class);
}