package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import com.google.gwt.dom.client.*;
import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

import com.kiouri.sliderbar.client.event.*;

import org.dspace.gwt.rpc.*;

public class DescriptorInputPanel extends Composite {

	private DescriptorColumn descriptor = null;

	private MathContext context = null;

	private BigDecimal value = null;

	private DescriptorValueLabel label = null;

	private DescriptorValueSliderBarHorizontal slider = null;


	public DescriptorInputPanel(final DescriptorColumn descriptor, final PredictionColumn training){
		setDescriptor(descriptor);

		final
		Map<String, ?> trainingValues = training.getValues();

		final
		Map<String, ?> trainingDescriptorValues = ParameterUtil.subset(trainingValues.keySet(), descriptor.getValues());
		setContext(MathUtil.getContext(trainingDescriptorValues.values()));

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
		this.label = new DescriptorValueLabel();
		this.label.setUserValue(getValue());

		return this.label;
	}

	private Widget createContent(DescriptorColumn descriptor, PredictionColumn training, Map<String, ?> trainingDescriptorValues, final BigDecimal mean, final BigDecimal sigma){
		Panel panel = new FlowPanel();

		QdbPlot.Bounds xBounds = QdbPlot.bounds(trainingDescriptorValues);

		this.slider = new DescriptorValueSliderBarHorizontal(xBounds);
		this.slider.setUserValue(getValue());

		this.slider.addMarkings(mean, sigma);

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

		HistogramPlot histogramPlot = new HistogramPlot(xBounds.getMin(), xBounds.getMax(), Math.max((int)Math.sqrt(trainingDescriptorValues.size()), 10));
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

		int scale = getScale();
		if(result.scale() > scale){
			result = result.setScale(scale, RoundingMode.HALF_UP);
		}

		return result;
	}

	private int getScale(){
		String format = getFormat();

		int dot = format.lastIndexOf('.');
		if(dot > -1){
			return (format.length() - (dot + 1));
		}

		return 0;
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