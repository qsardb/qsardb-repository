package org.dspace.qsardb.client.gwt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.kiouri.sliderbar.client.view.SliderBarHorizontal;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DescriptorValueSliderBarHorizontal extends SliderBarHorizontal {

	private final double min;
	private final double max;
	private final List<Marking> markings = new ArrayList<>();

	public DescriptorValueSliderBarHorizontal(QdbPlot.Bounds bounds) {
		this(bounds, QdbPlot.PLOT_SIZE);
	}

	public DescriptorValueSliderBarHorizontal(QdbPlot.Bounds bounds, int maxValue) {
		this.min = bounds.getMin().doubleValue();
		this.max = bounds.getMax().doubleValue();
		setMaxValue(maxValue);
		setWidth(String.valueOf(20 + QdbPlot.PLOT_SIZE + 20) + "px");

		setLessWidget(new Image(images.less()));
		setScaleWidget(new Image(images.scale().getSafeUri()), 15);
		setDragWidget(new Image(images.drag()));
		setMoreWidget(new Image(images.more()));
	}

	@Override
	protected void placeWidgets(ArrayList<Widget> widgets) {
		super.placeWidgets(widgets);

		for (Marking marking : this.markings) {
			super.absPanel.add(marking, super.startPosition + marking.getValue() - marking.getOffsetWidth() / 2, 0);
		}
	}

	public void addMarkings(BigDecimal mean, BigDecimal sigma) {
		addMeanMarking(mean);

		addStDevMarkings(mean, sigma, new BigDecimal(2), QdbPlot.COLOR_TWO_SIGMA);
		addStDevMarkings(mean, sigma, new BigDecimal(3), QdbPlot.COLOR_THREE_SIGMA);
	}

	private void addMeanMarking(BigDecimal mean) {
		addMarking(mean, "#000000", "Avg");
	}

	private void addStDevMarkings(BigDecimal mean, BigDecimal sigma, BigDecimal multiplier, String color) {
		addMarking(mean.add(sigma.multiply(multiplier)), color, ("+" + multiplier.intValue() + SIGMA));
		addMarking(mean.subtract(sigma.multiply(multiplier)), color, ("-" + multiplier.intValue() + SIGMA));
	}

	private void addMarking(Number value, String color, String title) {
		if (value.doubleValue() < min || value.doubleValue() > max) {
			return;
		}

		Marking marking = new Marking(color, toSliderValue(value));
		marking.setTitle(title + ": " + value.toString());

		this.markings.add(marking);
	}

	public Number getUserValue() {
		return min + (max - min) * getValue() / getMaxValue();
	}

	public void setUserValue(Number value) {
		setValue(toSliderValue(value));
	}

	private int toSliderValue(Number value) {
		double ratio = (value.doubleValue() - min) / (max - min);
		return (int) Math.max(0, Math.min(ratio*getMaxValue(), getMaxValue()));
	}

	private class Marking extends AbsolutePanel {
		private final int value;

		public Marking(String color, int value) {
			setPixelSize(2, 20);

			Element element = getElement();
			DOM.setStyleAttribute(element, "backgroundColor", color);

			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	private static final String SIGMA = "\u03C3";

	interface Images extends ClientBundle {
		@Source(value = "images/sliderDrag.png")
		ImageResource drag();

		@Source(value = "images/sliderLess.png")
		ImageResource less();

		@Source(value = "images/sliderMore.png")
		ImageResource more();

		@Source(value = "images/sliderScale.png")
		DataResource scale();
	}

	private static final Images images = GWT.create(Images.class);
}
