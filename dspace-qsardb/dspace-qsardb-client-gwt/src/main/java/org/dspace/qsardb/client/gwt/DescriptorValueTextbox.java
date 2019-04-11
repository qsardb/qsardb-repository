package org.dspace.qsardb.client.gwt;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TextBox;

public class DescriptorValueTextbox extends TextBox {

	private final Number mean;
	private final Number sigma;

	public DescriptorValueTextbox(Number mean, Number sigma) {
		this.mean = mean;
		this.sigma = sigma;

		Style style = getElement().getStyle();
		style.setPaddingLeft(3, Style.Unit.PX);
		style.setPaddingRight(3, Style.Unit.PX);
		style.setTextAlign(Style.TextAlign.RIGHT);
		style.setWidth(100, Style.Unit.PX);
		style.setBorderStyle(Style.BorderStyle.SOLID);
		style.setBorderColor("#e7e7e7");
	}

	public void setUserValue(Number value) {
		setValue(value.toString(), false);
		colourTextBox(value);
	}

	@Override
	public void setText(String text) {
		try {
			Double.parseDouble(text);
		} catch (NumberFormatException e) {
			Window.alert("Number required");
			return;
		}
		super.setText(text);
		colourTextBox(Double.parseDouble(text));
	}

	private void colourTextBox(Number value) {
		double delta = Math.abs(mean.doubleValue() - value.doubleValue()) / sigma.doubleValue();

		Style style = getElement().getStyle();

		if (delta > 3) {
			style.setBackgroundColor(QdbPlot.COLOR_THREE_SIGMA);
		} else if (delta > 2) {
			style.setBackgroundColor(QdbPlot.COLOR_TWO_SIGMA);
		} else {
			style.clearBackgroundColor();
		}
	}
}
