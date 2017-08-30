/*
*  Copyright (c) 2015 University of Tartu
*/
package org.dspace.qsardb.client.gwt;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.kiouri.sliderbar.client.event.BarValueChangedEvent;
import com.kiouri.sliderbar.client.event.BarValueChangedHandler;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import org.dspace.qsardb.rpc.gwt.DescriptorColumn;
import org.dspace.qsardb.rpc.gwt.PredictionColumn;
import org.dspace.qsardb.rpc.gwt.PropertyColumn;

public class DescriptorInputComponent extends Composite {
	
	@UiField(provided = true) DescriptorValueTextbox descriptorValue;
	@UiField(provided = true) ToggleButton collapseButton;
	@UiField(provided = true) FlowPanel collapsiblePanel;
	@UiField(provided = true) Label modelSoftLabel;
	@UiField(provided = true) Label predictionSoftLabel;
	
	private DescriptorValueSliderBarHorizontal slider;
	private DescriptorColumn descriptor = null;
	private MathContext context = null;
	private BigDecimal value = null;
	private boolean enableSlideEvents = false;
	
	private final PredictionColumn training;
	private final Map<String, ?> trainingDescriptorValues;
	private final BigDecimal mean;
	private final BigDecimal sigma;
	private QdbPlot.Bounds xBounds;
	
	private final HorizontalPanel expandFace;
	private final HorizontalPanel collapseFace;
	
	private static final Binder binder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, DescriptorInputComponent> {}
	final QdbPredictor predictor = (QdbPredictor)Application.getInstance();
	
	public DescriptorInputComponent(final PropertyColumn property, final DescriptorColumn descriptor, final PredictionColumn training) {
		
		this.training = training;
		collapseButton = new ToggleButton();
		
		Image ex = new Image(images.expand());
		Image cl = new Image(images.collapse());
		
		cl.getElement().setAttribute("style", "padding-left:0px; padding-right:5px; vertical-align:middle; padding-bottom:1px;");
		ex.getElement().setAttribute("style", "padding-left:0px; padding-right:5px; vertical-align:middle; padding-bottom:1px;");
		
		
		expandFace = new HorizontalPanel();
		collapseFace = new HorizontalPanel();
		
		collapseFace.getElement().setAttribute("style", "border: none; outline: none;");
		expandFace.getElement().setAttribute("style", "border: none; outline: none;");
		
		collapseFace.add(cl);
		expandFace.add(ex);
		
		String descriptorUnits = descriptor.getUnits();
		if (descriptorUnits != null && !descriptorUnits.trim().equals("")) {
			descriptorUnits = " [" + descriptorUnits + "]";
		} else {
			descriptorUnits = "";
		}
		
		Label expandLabel = new Label(descriptor.getId() + ": " + descriptor.getName() + descriptorUnits);
		Label collapseLabel = new Label(descriptor.getId() + ": " + descriptor.getName() + descriptorUnits);
		
		expandFace.add(expandLabel);
		collapseFace.add(collapseLabel);
		expandFace.setCellWidth(expandLabel, "100%");
		collapseFace.setCellWidth(collapseLabel, "100%");
		
		expandLabel.getElement().setAttribute("style", "white-space: normal;");
		collapseLabel.getElement().setAttribute("style", "white-space: normal;");
		
		
		collapseButton.getElement().appendChild(expandFace.getElement());
		
		
		setDescriptor(descriptor);
		ParameterUtil.ensureConverted(descriptor);
		
		final Map<String, ?> trainingValues = training.getValues();
		trainingDescriptorValues = ParameterUtil.subset(trainingValues.keySet(), descriptor.getValues());
		
		setContext(MathUtil.getContext(trainingDescriptorValues.values(), 8));
		
		mean = formatValue(MathUtil.mean(trainingDescriptorValues.values()));
		setValue(mean);
		
		sigma = formatValue(MathUtil.standardDeviation(trainingDescriptorValues.values()));
		
		descriptorValue = new DescriptorValueTextbox(mean, sigma);
		descriptorValue.getElement().getStyle().setProperty("float", "right");
		descriptorValue.setUserValue(value);
		
		modelSoftLabel = new Label();
		if ((descriptor.getApplication() == null || descriptor.getApplication().trim().equals(""))) {
			modelSoftLabel.setText("Descriptor in original model calculated with <N/A>");
		} else {
			modelSoftLabel.setText("Descriptor in original model calculated with " + descriptor.getApplication());
		}
		predictionSoftLabel = new Label("Current prediction is made with mean value");

		predictionSoftLabel.getElement().setAttribute("style", "white-space: normal;");
		modelSoftLabel.getElement().setAttribute("style", "white-space: normal;");

		collapsiblePanel = new FlowPanel();
		initWidget(binder.createAndBindUi(this));
		collapsiblePanel.setVisible(false);
	}
	

    	public HandlerRegistration addInputChangeEventHandler(InputChangeEventHandler handler){
		return addHandler(handler, InputChangeEvent.TYPE);
	}
	
	
	private Widget createPanel() {
		Panel panel = new FlowPanel();
		
		panel.add(modelSoftLabel);
		panel.add(predictionSoftLabel);

		xBounds = QdbPlot.bounds(trainingDescriptorValues);
		
		BigDecimal range = xBounds.getRange();
		
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
			slider = new DescriptorValueSliderBarHorizontal(xBounds, categories);
		} else {
			slider = new DescriptorValueSliderBarHorizontal(xBounds);
			slider.addMarkings(mean, sigma);
		}
		
		Style sliderStyle = (this.slider.getElement()).getStyle();
		sliderStyle.setMarginTop(3, Style.Unit.PX);
		sliderStyle.setMarginBottom(3, Style.Unit.PX);
		
		slider.setUserValue(value);

		HistogramPlot histogramPlot;
		
		if(categories > 0){
			Double min = Double.valueOf((xBounds.getMin()).intValue() - 0.5);
			Double max = Double.valueOf((xBounds.getMax()).intValue() + 0.5);
			
			histogramPlot = new HistogramPlot(min, max, categories + 1);
		} else {
			histogramPlot = new HistogramPlot(xBounds.getMin(), xBounds.getMax(), Math.max((int)Math.sqrt(trainingDescriptorValues.size()), 10));
		}
		
		histogramPlot.addXAxisOptions(xBounds);
		histogramPlot.addYAxisOptions((String)null);
		
		histogramPlot.addSeries(PredictionSeries.create(training), trainingDescriptorValues);
		
		
		panel.add(slider);
		panel.add(histogramPlot);
		
		histogramPlot.changeSeriesVisibility(new SeriesDisplayEvent(Collections.singletonMap(training, Boolean.TRUE)));
		
		return panel;
	}
    
    
    
	@UiHandler("descriptorValue")
	void handleDescriptorValue(ValueChangeEvent<String> evt) {
		enableSlideEvents = false;
		try {
			predictor.getDataInputPanel().cleanCompoundData();
			try {
				value = formatValue(new BigDecimal(evt.getValue()));
			} catch (NumberFormatException e) {
					return;
			}
			value = formatValue(new BigDecimal(evt.getValue()));
			if (slider != null) {
				slider.setUserValue(value);
				if (!outOfBounds(value)) {
					//TODO: move inside slider, should handle this by itself
					slider.normaliseMoreAndLess();
				}
			}
			
			fireDescriptorValueChangedEvent();
		} finally {
			enableSlideEvents = true;
			predictionSoftLabel.setText("Current prediction is made with value entered by user");
		}
		
	}
				
				
	@UiHandler("collapseButton")
	void handleClick(ClickEvent evt) {
		if (collapsiblePanel.isVisible()) {
			collapsiblePanel.setVisible(false);
			collapseButton.getElement().removeAllChildren();
			collapseButton.getElement().appendChild(expandFace.getElement());
			
		} else {
			collapsiblePanel.setVisible(true);
			
			collapseButton.getElement().removeAllChildren();
			collapseButton.getElement().appendChild(collapseFace.getElement());
			
			if (slider == null) {
				collapsiblePanel.add(createPanel());
				
				//adding this here, if in constructor, pics up the first event and messes things up
				BarValueChangedHandler valueHandler = new BarValueChangedHandler(){
					
					@Override
					public void onBarValueChanged(BarValueChangedEvent event){
						if (enableSlideEvents) {
							value = formatValue(slider.getUserValue());
							descriptorValue.setUserValue(value);
							predictor.getDataInputPanel().cleanCompoundData();
							
							predictionSoftLabel.setText("Current prediction is made with value entered by user");

							//TODO: move inside slider, should handle this by itself
							slider.normaliseMoreAndLess();
							fireDescriptorValueChangedEvent();
						}
					}
				};
				
				slider.addBarValueChangedHandler(valueHandler);
				enableSlideEvents = true;
			}
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
	
	private boolean outOfBounds(Number value) {
		BigDecimal min = xBounds.getMin();
		BigDecimal max = xBounds.getMax();
		
		if (min != null && max != null && (value.doubleValue() >= min.doubleValue()) && (value.doubleValue() <= max.doubleValue())) {
			return false;
		} else {
			return true;
		}
		
	}
	
	private void fireDescriptorValueChangedEvent(){
		DescriptorColumn descriptor = getDescriptor();
		fireEvent(new DescriptorValueChangeEvent(descriptor));
	}
	
	public HandlerRegistration addDescriptorValueChangeEventHandler(DescriptorValueChangeEventHandler handler){
		return addHandler(handler, DescriptorValueChangeEvent.TYPE);
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
		
		if(this.slider == null){
			if(this.descriptorValue != null){
				this.descriptorValue.setValue(value.toPlainString(), false);
			}
		}
		if(this.slider != null){
			slider.setUserValue(value);
		} 
		if(this.descriptorValue != null){
			this.descriptorValue.setValue(value.toPlainString(), false);
		}
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
	
	public boolean isEnableSlideEvents() {
		return enableSlideEvents;
	}
	
	public void setEnableSlideEvents(boolean enableSlideEvents) {
		this.enableSlideEvents = enableSlideEvents;
	}
	
	public DescriptorValueSliderBarHorizontal getSlider() {
		return slider;
	}
	
	
	interface Images extends ClientBundle {
		
		@ClientBundle.Source (
			value = "com/google/gwt/user/client/ui/disclosurePanelClosed.png"
		)
		ImageResource expand();
							
		@ClientBundle.Source (
			value = "com/google/gwt/user/client/ui/disclosurePanelOpen.png"
		)
		ImageResource collapse();
	}
	
	private static final Images images = com.google.gwt.core.client.GWT.create(Images.class);
}
