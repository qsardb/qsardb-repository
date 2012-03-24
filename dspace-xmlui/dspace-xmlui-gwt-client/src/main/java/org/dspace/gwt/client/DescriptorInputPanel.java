package org.dspace.gwt.client;

import java.math.*;

import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

import com.kiouri.sliderbar.client.event.*;
import com.kiouri.sliderbar.client.solution.kde.*;
import com.kiouri.sliderbar.client.view.*;

import org.dspace.gwt.rpc.*;

public class DescriptorInputPanel extends Composite {

	private DescriptorColumn descriptor = null;

	private QdbPlot.Bounds bounds = null;

	private BigDecimal value = null;

	private SliderBarHorizontal slider = null;


	public DescriptorInputPanel(final DescriptorColumn descriptor){
		setDescriptor(descriptor);

		this.bounds = QdbPlot.bounds(descriptor.getValues());

		FlexTable table = new FlexTable();

		FlexTable.FlexCellFormatter formatter = (FlexTable.FlexCellFormatter)table.getCellFormatter();

		final
		Label name = new Label(descriptor.getName());

		table.setWidget(0, 0, name);
		formatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);

		final
		Label value = new Label("");
		table.setWidget(1, 0, value);

		formatter.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);

		this.slider = new KDEHorizontalLeftBW(500, "500px");
		table.setWidget(2, 0, this.slider);

		BarValueChangedHandler valueHandler = new BarValueChangedHandler(){

			@Override
			public void onBarValueChanged(BarValueChangedEvent event){
				updateValue();

				value.setText(String.valueOf(getValue()));

				// XXX
				fireEvent(new DescriptorValueChangeEvent(descriptor));
			}
		};
		this.slider.addBarValueChangedHandler(valueHandler);

		initWidget(table);
	}

	public HandlerRegistration addDescriptorValueChangeEventHandler(DescriptorValueChangeEventHandler handler){
		return addHandler(handler, DescriptorValueChangeEvent.TYPE);
	}

	public MathContext getMathContext(){
		return this.bounds.getMathContext();
	}

	private void updateValue(){
		MathContext context = this.bounds.getMathContext();

		BigDecimal min = this.bounds.getMin();
		BigDecimal max = this.bounds.getMax();

		BigDecimal sliderValue = new BigDecimal(this.slider.getValue());
		BigDecimal sliderMaxValue = new BigDecimal(this.slider.getMaxValue());

		BigDecimal value = (min).add((max.subtract(min)).multiply(sliderValue).divide(sliderMaxValue, context));
		value = value.setScale(this.bounds.scale(), RoundingMode.HALF_UP);

		this.value = value;
	}

	public BigDecimal getValue(){
		return this.value;
	}

	public void setValue(String value){
		MathContext context = this.bounds.getMathContext();

		setValue(new BigDecimal(value, context));
	}

	public void setValue(BigDecimal value){
		MathContext context = this.bounds.getMathContext();

		BigDecimal min = this.bounds.getMin();
		BigDecimal max = this.bounds.getMax();

		BigDecimal sliderMaxValue = new BigDecimal(this.slider.getMaxValue());

		if(value != null){
			value = this.bounds.validate(value);

			BigDecimal sliderValue = ((value).subtract(min)).divide(max.subtract(min), context).multiply(sliderMaxValue);

			this.slider.setValue(sliderValue.intValue());
		}

		this.value = value;
	}

	public String getId(){
		return getDescriptor().getId();
	}

	public DescriptorColumn getDescriptor(){
		return this.descriptor;
	}

	private void setDescriptor(DescriptorColumn descriptor){
		this.descriptor = descriptor;
	}
}