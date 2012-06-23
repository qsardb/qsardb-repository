package org.dspace.client.gwt;

import java.math.*;
import java.util.*;

import com.google.gwt.dom.client.*;
import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

import com.kiouri.sliderbar.client.event.*;

import org.dspace.rpc.gwt.*;

public class DescriptorInputPanel extends Composite {

	private DescriptorColumn descriptor = null;

	private MathContext context = null;

	private BigDecimal value = null;

	private DescriptorValueLabel label = null;

	private DescriptorValueSliderBarHorizontal slider = null;


	public DescriptorInputPanel(final PropertyColumn property, final DescriptorColumn descriptor, final PredictionColumn training){
		setDescriptor(descriptor);

		// XXX
		ParameterUtil.ensureConverted(descriptor);

		final
		Map<String, ?> trainingValues = training.getValues();

		final
		Map<String, ?> trainingDescriptorValues = ParameterUtil.subset(trainingValues.keySet(), descriptor.getValues());

		setContext(MathUtil.getContext(trainingDescriptorValues.values(), 8));

		final
		BigDecimal mean = formatValue(MathUtil.mean(trainingDescriptorValues.values()));
		setValue(mean);

		final
		BigDecimal sigma = formatValue(MathUtil.standardDeviation(trainingDescriptorValues.values()));

		DisclosurePanel panel = new DisclosurePanel();

		LazyHeader header = new LazyHeader(panel){

			@Override
			public Label createLeft(){
				return new Label(descriptor.getName());
			}

			@Override
			public Label createRight(){
				return createValueLabel(descriptor, mean, sigma);
			}
		};
		panel.setHeader(header);

		header.ensureWidget();

		LazyContent content = new LazyContent(panel){

			@Override
			public Widget createWidget(){
				return createContent(descriptor, training, trainingDescriptorValues, mean, sigma);
			}
		};
		panel.setContent(content);

		initWidget(panel);
	}

	private Label createValueLabel(final DescriptorColumn descriptor, final BigDecimal mean, final BigDecimal sigma){
		this.label = new DescriptorValueLabel(mean, sigma);
		this.label.setUserValue(getValue());

		return this.label;
	}

	private Widget createContent(DescriptorColumn descriptor, PredictionColumn training, Map<String, ?> trainingDescriptorValues, final BigDecimal mean, final BigDecimal sigma){
		Panel panel = new FlowPanel();

		QdbPlot.Bounds xBounds = QdbPlot.bounds(trainingDescriptorValues);

		BigDecimal range = xBounds.getRange();

		// XXX
		if((range).compareTo(BigDecimal.ZERO) == 0){
			return new Label("(Not adjustable)");
		}

		int categories = 0;

		int scale = MathUtil.getScale(getFormat());
		if(scale == 0){

			if(range.intValue() <= 10){
				categories = range.intValue();
			}
		} // End if

		if(categories > 0){
			this.slider = new DescriptorValueSliderBarHorizontal(xBounds, categories);
		} else

		{
			this.slider = new DescriptorValueSliderBarHorizontal(xBounds);
			this.slider.addMarkings(mean, sigma);
		}

		this.slider.setUserValue(getValue());

		panel.add(this.slider);

		Style sliderStyle = (this.slider.getElement()).getStyle();

		sliderStyle.setMarginTop(3, Style.Unit.PX);
		sliderStyle.setMarginBottom(3, Style.Unit.PX);

		BarValueChangedHandler valueHandler = new BarValueChangedHandler(){

			@Override
			public void onBarValueChanged(BarValueChangedEvent event){
				updateValue();

				fireDescriptorValueChangedEvent();
			}
		};
		this.slider.addBarValueChangedHandler(valueHandler);

		HistogramPlot histogramPlot;

		if(categories > 0){
			Double min = Double.valueOf((xBounds.getMin()).intValue() - 0.5);
			Double max = Double.valueOf((xBounds.getMax()).intValue() + 0.5);

			histogramPlot = new HistogramPlot(min, max, categories + 1);
		} else

		{
			histogramPlot = new HistogramPlot(xBounds.getMin(), xBounds.getMax(), Math.max((int)Math.sqrt(trainingDescriptorValues.size()), 10));
		}

		histogramPlot.addXAxisOptions(xBounds);
		histogramPlot.addYAxisOptions((String)null);

		histogramPlot.addSeries(new PredictionSeries(training), trainingDescriptorValues);

		panel.add(histogramPlot);

		return panel;
	}

	public HandlerRegistration addDescriptorValueChangeEventHandler(DescriptorValueChangeEventHandler handler){
		return addHandler(handler, DescriptorValueChangeEvent.TYPE);
	}

	private void fireDescriptorValueChangedEvent(){
		DescriptorColumn descriptor = getDescriptor();

		fireEvent(new DescriptorValueChangeEvent(descriptor));
	}

	public MathContext getContext(){
		return this.context;
	}

	private void setContext(MathContext context){
		this.context = context;
	}

	public BigDecimal getValue(){
		return this.value;
	}

	public void setValue(String value){
		setValue(formatValue(Double.valueOf(value)));
	}

	public void setValue(BigDecimal value){
		this.value = value;

		if(this.label != null){
			this.label.setUserValue(value);
		} // End if

		if(this.slider != null){
			this.slider.setUserValue(value);
		}
	}

	private void updateValue(){
		Number value = this.slider.getUserValue();

		updateValue(formatValue(value));
	}

	private void updateValue(BigDecimal value){
		this.value = value;

		if(this.label != null){
			this.label.setUserValue(value);
		}
	}

	private BigDecimal formatValue(Number value){
		BigDecimal result = new BigDecimal(value.doubleValue(), getContext());

		int scale = MathUtil.getScale(getFormat());

		if(result.scale() > scale){
			result = result.setScale(scale, RoundingMode.HALF_UP);
		}

		return result;
	}

	public String getId(){
		return getDescriptor().getId();
	}

	public String getFormat(){
		return getDescriptor().getFormat();
	}

	public DescriptorColumn getDescriptor(){
		return this.descriptor;
	}

	private void setDescriptor(DescriptorColumn descriptor){
		this.descriptor = descriptor;
	}
}