/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dspace.qsardb.client.gwt;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

/**
 *
 * @author kaliif
 */


public class DescriptorValueTextbox extends TextBox {

	private Number mean = null;

	private Number sigma = null;
        
	public DescriptorValueTextbox(Number mean, Number sigma){
		
                setMean(mean);
		setSigma(sigma);
                
                
		Style style = getElement().getStyle();

		style.setPaddingLeft(3, Style.Unit.PX);
		style.setPaddingRight(3, Style.Unit.PX);
		style.setTextAlign(Style.TextAlign.RIGHT);
		style.setWidth(100, Style.Unit.PX);
		style.setBorderStyle(Style.BorderStyle.SOLID);
		style.setBorderColor("#e7e7e7");
		
		
		addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				setText(event.getValue());
			}
		});
		
	}
        
     
	public void setUserValue(Number value){
		setText(value.toString());
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
                
		if(delta >= 3){
			style.setBackgroundColor(QdbPlot.COLOR_THREE_SIGMA);
		} else

		if(delta >= 2){
			style.setBackgroundColor(QdbPlot.COLOR_TWO_SIGMA);
		} else {
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