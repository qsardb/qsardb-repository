package org.dspace.gwt.client;

import java.math.*;

import com.google.gwt.core.client.*;
import com.google.gwt.resources.client.*;
import com.google.gwt.user.client.ui.*;

import com.kiouri.sliderbar.client.view.*;

public class DescriptorSliderBarHorizontal extends SliderBarHorizontal {

	private QdbPlot.Bounds bounds = null;


	public DescriptorSliderBarHorizontal(int maxValue, QdbPlot.Bounds bounds){
		setMaxValue(maxValue);

		setLessWidget(new Image(images.less()));
		setScaleWidget(new Image(images.scale().getSafeUri()), 15);
		setMoreWidget(new Image(images.more()));

		setDragWidget(new Image(images.drag()));

		setBounds(bounds);
	}

	public MathContext getMathContext(){
		return this.bounds.getMathContext();
	}

	public BigDecimal getUserValue(){
		MathContext context = getMathContext();

		BigDecimal min = this.bounds.getMin();
		BigDecimal max = this.bounds.getMax();

		BigDecimal sliderMaxValue = new BigDecimal(getMaxValue());
		BigDecimal sliderValue = new BigDecimal(getValue());

		BigDecimal value = (min).add((max.subtract(min)).multiply(sliderValue).divide(sliderMaxValue, context));

		return value.setScale(context.getPrecision(), context.getRoundingMode());
	}

	public void setUserValue(BigDecimal value){
		MathContext context = getMathContext();

		value = this.bounds.validate(value);

		BigDecimal min = this.bounds.getMin();
		BigDecimal max = this.bounds.getMax();

		BigDecimal sliderMaxValue = new BigDecimal(getMaxValue());
		BigDecimal sliderValue = ((value).subtract(min)).divide(max.subtract(min), context).multiply(sliderMaxValue);

		setValue(sliderValue.intValue());
	}

	public QdbPlot.Bounds getBounds(){
		return this.bounds;
	}

	private void setBounds(QdbPlot.Bounds bounds){
		this.bounds = bounds;
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