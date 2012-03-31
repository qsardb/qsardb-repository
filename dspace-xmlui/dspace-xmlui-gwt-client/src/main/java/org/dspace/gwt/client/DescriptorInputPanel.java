package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

import com.kiouri.sliderbar.client.event.*;

import org.dspace.gwt.rpc.*;

public class DescriptorInputPanel extends Composite {

	private DescriptorColumn descriptor = null;

	private DescriptorSliderBarHorizontal slider = null;


	public DescriptorInputPanel(final DescriptorColumn descriptor, PredictionColumn training){
		setDescriptor(descriptor);

		Map<String, ?> trainingValues = training.getValues();

		Map<String, ?> trainingDescriptorValues = ParameterUtil.subset(trainingValues.keySet(), descriptor.getValues());

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

		QdbPlot.Bounds xBounds = QdbPlot.bounds(trainingDescriptorValues);

		this.slider = new DescriptorSliderBarHorizontal(300, xBounds);
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

		final
		HistogramPlot histogramPlot = new HistogramPlot(xBounds.getMin(), xBounds.getMax(), Math.max((int)Math.sqrt(trainingDescriptorValues.size()), 10));
		histogramPlot.setXAxisBounds(xBounds);

		histogramPlot.addSeries(new PredictionSeries(training), trainingDescriptorValues);

		QdbPlot.Bounds yBounds = new QdbPlot.Bounds();
		yBounds.setMin(BigDecimal.ZERO);
		yBounds.setMax(new BigDecimal(histogramPlot.getMaxHeight()));

		histogramPlot.setYAxisBounds(yBounds);

		table.setWidget(3, 0, histogramPlot);

		histogramPlot.setVisible(false);

		MouseFocusHandler focusHandler = new MouseFocusHandler(){

			@Override
			public void focusChanged(boolean focus){
				histogramPlot.setVisible(focus);
			}
		};
		focusHandler.install(this.slider);

		Double mean = MathUtil.mean(trainingDescriptorValues.values());

		setValue(mean.toString());

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