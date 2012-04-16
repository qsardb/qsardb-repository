package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import com.google.gwt.core.client.*;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.*;
import com.google.gwt.resources.client.*;
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
		BigDecimal mean = new BigDecimal(MathUtil.mean(trainingDescriptorValues.values()).toString(), getContext());
		setValue(mean);

		final
		BigDecimal sigma = new BigDecimal(MathUtil.standardDeviation(trainingDescriptorValues.values()).toString(), getContext());

		final
		DisclosurePanel panel = new DisclosurePanel();
		panel.setHeader(createHeader(panel, descriptor, mean, sigma));

		OpenHandler<DisclosurePanel> openHandler = new OpenHandler<DisclosurePanel>(){

			@Override
			public void onOpen(OpenEvent<DisclosurePanel> event){

				if(panel.getContent() != null){
					return;
				}

				panel.setContent(createContent(panel, descriptor, training, trainingDescriptorValues, mean, sigma));
			}
		};
		panel.addOpenHandler(openHandler);

		initWidget(panel);
	}

	private Widget createHeader(DisclosurePanel parent, DescriptorColumn descriptor, final BigDecimal mean, final BigDecimal sigma){
		Panel panel = new FlowPanel();

		final
		Image image = new Image(images.expand());

		panel.add(image);

		Style imageStyle = (image.getElement()).getStyle();

		imageStyle.setFloat(Style.Float.LEFT);

		imageStyle.setMarginTop(1, Style.Unit.PX);
		imageStyle.setMarginRight(3, Style.Unit.PX);
		imageStyle.setMarginBottom(1, Style.Unit.PX);

		OpenHandler<DisclosurePanel> openHandler = new OpenHandler<DisclosurePanel>(){

			@Override
			public void onOpen(OpenEvent<DisclosurePanel> event){
				image.setResource(images.collapse());
			}
		};
		parent.addOpenHandler(openHandler);

		CloseHandler<DisclosurePanel> closeHandler = new CloseHandler<DisclosurePanel>(){

			@Override
			public void onClose(CloseEvent<DisclosurePanel> event){
				image.setResource(images.expand());
			}
		};
		parent.addCloseHandler(closeHandler);

		Label name = new Label(descriptor.getName());

		panel.add(name);

		(name.getElement()).getStyle().setFloat(Style.Float.LEFT);

		this.label = new DescriptorValueLabel();
		this.label.setUserValue(getValue());

		panel.add(this.label);

		(this.label.getElement()).getStyle().setFloat(Style.Float.RIGHT);

		return panel;
	}

	private Widget createContent(DisclosurePanel parent, DescriptorColumn descriptor, PredictionColumn training, Map<String, ?> trainingDescriptorValues, final BigDecimal mean, final BigDecimal sigma){
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

		final
		HistogramPlot histogramPlot = new HistogramPlot(xBounds.getMin(), xBounds.getMax(), Math.max((int)Math.sqrt(trainingDescriptorValues.size()), 10));
		histogramPlot.addXAxisOptions(xBounds);

		histogramPlot.addSeries(new PredictionSeries(training), trainingDescriptorValues);

		QdbPlot.Bounds yBounds = new QdbPlot.Bounds();
		yBounds.setMin(BigDecimal.ZERO);
		yBounds.setMax(new BigDecimal(histogramPlot.getMaxHeight()));

		histogramPlot.addYAxisOptions(yBounds);

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
		setValue(new BigDecimal(value, getContext()));
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

		updateValue(new BigDecimal(String.valueOf(value), getContext()));
	}

	private void updateValue(BigDecimal value){
		this.value = value;

		if(this.label != null){
			this.label.setUserValue(value);
		}
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

	interface Images extends ClientBundle {

		@Source (
			value = "com/google/gwt/user/client/ui/disclosurePanelClosed.png"
		)
		ImageResource expand();

		@Source (
			value = "com/google/gwt/user/client/ui/disclosurePanelOpen.png"
		)
		ImageResource collapse();
	}

	private static final Images images = GWT.create(Images.class);
}