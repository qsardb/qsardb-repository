package org.dspace.qsardb.client.gwt;

import com.google.gwt.dom.client.*;
import com.google.gwt.user.client.ui.*;

public class DescriptorValueLabel extends Label {

	private Number mean = null;

	private Number sigma = null;


	public DescriptorValueLabel(Number mean, Number sigma){
		setMean(mean);
		setSigma(sigma);

		// XXX
		Style style = getElement().getStyle();

		style.setPaddingLeft(3, Style.Unit.PX);
		style.setPaddingRight(3, Style.Unit.PX);
	}

	public void setUserValue(Number value){
		setText(value.toString());

		Number mean = getMean();
		Number sigma = getSigma();

		double delta = Math.abs(mean.doubleValue() - value.doubleValue()) / sigma.doubleValue();

		// XXX
		Style style = getElement().getStyle();

		if(delta >= 3){
			style.setBackgroundColor(QdbPlot.COLOR_THREE_SIGMA);
		} else

		if(delta >= 2){
			style.setBackgroundColor(QdbPlot.COLOR_TWO_SIGMA);
		} else

		{
			style.clearBackgroundColor();
		}
	}

	public Number getMean(){
		return this.mean;
	}

	private void setMean(Number mean){
		this.mean = mean;
	}

	public Number getSigma(){
		return this.sigma;
	}

	private void setSigma(Number sigma){
		this.sigma = sigma;
	}
}