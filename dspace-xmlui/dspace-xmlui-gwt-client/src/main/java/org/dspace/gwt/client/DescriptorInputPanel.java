package org.dspace.gwt.client;

import java.math.*;

import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

import com.kiouri.sliderbar.client.event.*;

import org.dspace.gwt.rpc.*;

public class DescriptorInputPanel extends Composite {

	private DescriptorColumn descriptor = null;

	private DescriptorSliderBarHorizontal slider = null;


	public DescriptorInputPanel(final DescriptorColumn descriptor){
		setDescriptor(descriptor);

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

		QdbPlot.Bounds bounds = QdbPlot.bounds(descriptor.getValues());

		this.slider = new DescriptorSliderBarHorizontal(500, bounds);
		this.slider.setWidth("500px");

		table.setWidget(2, 0, this.slider);

		BarValueChangedHandler valueHandler = new BarValueChangedHandler(){

			@Override
			public void onBarValueChanged(BarValueChangedEvent event){
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

	public BigDecimal getValue(){
		return this.slider.getUserValue();
	}

	public void setValue(String value){
		MathContext context = this.slider.getMathContext();

		setValue(new BigDecimal(value, context));
	}

	public void setValue(BigDecimal value){
		this.slider.setUserValue(value);
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