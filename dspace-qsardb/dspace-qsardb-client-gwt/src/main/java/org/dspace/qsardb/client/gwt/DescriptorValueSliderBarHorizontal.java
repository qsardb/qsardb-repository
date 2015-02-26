package org.dspace.qsardb.client.gwt;

import java.math.*;
import java.util.*;

import com.google.gwt.core.client.*;
import com.google.gwt.dom.client.Style;
import com.google.gwt.resources.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import com.kiouri.sliderbar.client.view.*;

public class DescriptorValueSliderBarHorizontal extends SliderBarHorizontal {
	
	private QdbPlot.Bounds bounds = null;
	
	private final List<Marking> markings = new ArrayList<Marking>();
	
	private SliderButton less;
	
	private SliderButton more;
	
	private final BigDecimal min;
	
	private final BigDecimal max;
	
	
	public DescriptorValueSliderBarHorizontal(QdbPlot.Bounds bounds){
		this(bounds, QdbPlot.PLOT_SIZE);
	}
	
	public DescriptorValueSliderBarHorizontal(QdbPlot.Bounds bounds, int maxValue){
		setBounds(bounds);
		
		min = bounds.getMin();
		max = bounds.getMax();
		
		less = new SliderButton(new Image(images.less()), new Image(images.lessOOB()));
		setLessWidget(less);
		
		setScaleWidget(new Image(images.scale().getSafeUri()), 15);
		
		Image drag = new Image(images.drag());
		setDragWidget(drag);
		
		setMaxValue(maxValue);
		
		setWidth(String.valueOf(20 + QdbPlot.PLOT_SIZE + 20) + "px");
		
		more = new SliderButton(new Image(images.more()), new Image(images.moreOOB()));
		setMoreWidget(more);
		
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
		return Double.valueOf(min.doubleValue() + (max.doubleValue() - min.doubleValue()) * value / getMaxValue());
	}
	
	
	public void setUserValue(Number value){
		if (min != null && value.doubleValue() < min.doubleValue()){
			if (!less.isOutofBounds()) {
				less.setOutOfBounds();
			}
			if (more.isOutofBounds()) {
				more.setNormal();
			}
			
		} else if (max != null && value.doubleValue() > max.doubleValue()){
			if (!more.isOutofBounds()) {
				more.setOutOfBounds();
			}
			if (less.isOutofBounds()) {
				less.setNormal();
			}
			
		} 
		setValue(toSliderValue(value));
	}
	
	
	public void normaliseMoreAndLess() {
		more.setNormal();
		less.setNormal();
	}
	
	private int toSliderValue(Number value){
		return (int)((value.doubleValue() - min.doubleValue()) / (max.doubleValue() - min.doubleValue()) * getMaxValue());
		}
	
	private Number validate(Number value, boolean replace){
		
		if(min != null && value.doubleValue() < min.doubleValue()){
			
			if(replace){
				return min;
			}
			
			throw new IllegalArgumentException();
		}
		
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
			value = "images/sliderLessOOB.png"
		)
		ImageResource lessOOB();
		
		@Source (
			value = "images/sliderMoreOOB.png"
		)
		ImageResource moreOOB();
		
		@Source (
			value = "images/sliderScale.png"
		)
		DataResource scale();
	}
	
	private static final Images images = GWT.create(Images.class);
}

class SliderButton extends PushButton {
	
	private final Image normalImage;
	private final Image outOfBoundsImage;
	
	private boolean outOfBounds;
	
	SliderButton(Image normalImage, Image outOfBoundsImage) {
		this.normalImage = normalImage;
		this.outOfBoundsImage = outOfBoundsImage;
		
		setWidth("20px");
		setHeight("20px");
		
		getElement().getStyle().setPadding(0, Style.Unit.PX);
		getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
		
		getUpFace().setImage(normalImage);
		
		outOfBounds = false;
		
	}
	
	void setOutOfBounds() {
		this.getUpFace().setImage(outOfBoundsImage);
		outOfBounds = true;
	}
	
	void setNormal() {
		this.getUpFace().setImage(normalImage);
		outOfBounds = false;
	}

	public boolean isOutofBounds() {
		return outOfBounds;
	}
	
}