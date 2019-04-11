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
import com.google.gwt.user.client.ui.InlineLabel;
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

	@UiField FlowPanel inputPanel;
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

	interface Binder extends UiBinder<Widget, DescriptorInputComponent> {
	}
	final QdbPredictor predictor = (QdbPredictor) Application.getInstance();

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

		String labelText = descriptor.getId() + ": " + descriptor.getName() + descriptorUnits;
		InlineLabel expandLabel = new InlineLabel(labelText);
		InlineLabel collapseLabel = new InlineLabel(labelText);

		expandFace.add(expandLabel);
		collapseFace.add(collapseLabel);

		expandLabel.getElement().setAttribute("style", "white-space: normal;");
		collapseLabel.getElement().setAttribute("style", "white-space: normal;");

		collapseButton.getElement().appendChild(expandFace.getElement());

		setDescriptor(descriptor);

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
		String modelSoft = descriptor.getApplication();
		if (modelSoft == null || modelSoft.trim().isEmpty()) {
			modelSoft = "<N/A>";
		}
		modelSoftLabel.setText("Descriptor values in the original model were calculated with " + modelSoft);
		modelSoftLabel.getElement().setAttribute("style", "white-space: normal;");

		predictionSoftLabel = new Label("This value is the mean descriptor value");
		predictionSoftLabel.getElement().setAttribute("style", "white-space: normal;");

		collapsiblePanel = new FlowPanel();
		initWidget(binder.createAndBindUi(this));

		collapsiblePanel.setVisible(false);

		if (descriptor.getDescription() != null) {
			DescriptionLabel descLabel = new DescriptionLabel(descriptor);
			descLabel.getElement().getStyle().setProperty("float", "left");
			inputPanel.insert(descLabel, 1);
		}
	}

	public HandlerRegistration addInputChangeEventHandler(InputChangeEventHandler handler) {
		return addHandler(handler, InputChangeEvent.TYPE);
	}

	private Widget createPanel() {
		Panel panel = new FlowPanel();

		panel.add(modelSoftLabel);
		panel.add(predictionSoftLabel);

		xBounds = QdbPlot.bounds(trainingDescriptorValues);

		BigDecimal range = xBounds.getRange();

		if (range.compareTo(BigDecimal.ZERO) == 0) {
			return new Label("(Not adjustable)");
		}

		int categories = 0;

		int scale = MathUtil.getScale(getFormat());
		if (scale == 0) {
			if (range.intValue() <= 10) {
				categories = range.intValue();
			}
		}

		if (categories > 0) {
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

		if (categories > 0) {
			Double min = xBounds.getMin().intValue() - 0.5;
			Double max = xBounds.getMax().intValue() + 0.5;

			histogramPlot = new HistogramPlot(min, max, categories + 1);
		} else {
			histogramPlot = new HistogramPlot(xBounds.getMin(), xBounds.getMax(), Math.max((int) Math.sqrt(trainingDescriptorValues.size()), 10));
		}

		histogramPlot.addXAxisOptions(xBounds);
		histogramPlot.addYAxisOptions((String) null);

		histogramPlot.addSeries(PredictionSeries.create(training), trainingDescriptorValues);

		panel.add(slider);
		panel.add(histogramPlot);

		histogramPlot.changeSeriesVisibility(new SeriesDisplayEvent(Collections.singletonMap(training, Boolean.TRUE)));

		BarValueChangedHandler valueHandler = new BarValueChangedHandler() {
			private int last = -1;

			@Override
			public void onBarValueChanged(BarValueChangedEvent event) {
				if (!enableSlideEvents || last == event.getValue()) {
					return;
				}

				last = event.getValue();

				predictor.getDataInputPanel().cleanCompoundData();

				value = formatValue(slider.getUserValue());
				descriptorValue.setValue(value.toPlainString(), false);

				predictionSoftLabel.setText("This value is entered by the user");

				fireDescriptorValueChangedEvent();
			}
		};
		
		slider.addBarValueChangedHandler(valueHandler);
		
		return panel;
	}

	@UiHandler("descriptorValue")
	void handleDescriptorValue(ValueChangeEvent<String> evt) {
		try {
			predictor.getDataInputPanel().cleanCompoundData();

			setValue(formatValue(new BigDecimal(evt.getValue())));
			predictionSoftLabel.setText("This value is entered by the user");

			fireDescriptorValueChangedEvent();
		} catch (NumberFormatException e) {
			// ignored
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
				enableSlideEvents = false;
				try {
					collapsiblePanel.add(createPanel());
				} finally {
					enableSlideEvents = true;
				}
			}
		}
	}

	private BigDecimal formatValue(Number value) {
		BigDecimal result = new BigDecimal(value.doubleValue(), getContext());

		int scale = MathUtil.getScale(getFormat());

		if (result.scale() > scale) {
			result = result.setScale(scale, RoundingMode.HALF_UP);
		}

		return result;
	}

	private void fireDescriptorValueChangedEvent() {
		fireEvent(new DescriptorValueChangeEvent());
	}

	public HandlerRegistration addDescriptorValueChangeEventHandler(DescriptorValueChangeEvent.Handler handler) {
		return addHandler(handler, DescriptorValueChangeEvent.TYPE);
	}

	private MathContext getContext() {
		return this.context;
	}

	private void setContext(MathContext context) {
		this.context = context;
	}

	public BigDecimal getValue() {
		return this.value;
	}

	public void setValue(String value) {
		setValue(formatValue(Double.valueOf(value)));
	}

	private void setValue(BigDecimal value) {
		boolean oldValue = enableSlideEvents;
		enableSlideEvents = false;

		try {
			this.value = value;

			if (descriptorValue != null) {
				descriptorValue.setValue(value.toPlainString(), false);
			}

			if (slider != null) {
				slider.setUserValue(value);
			}
		} finally {
			enableSlideEvents = oldValue;
		}
	}

	public void setDescriptorSource(String source) {
		predictionSoftLabel.setText(source);
	}

	public String getId() {
		return getDescriptor().getId();
	}

	private String getFormat() {
		return getDescriptor().getFormat();
	}

	public DescriptorColumn getDescriptor() {
		return this.descriptor;
	}

	private void setDescriptor(DescriptorColumn descriptor) {
		this.descriptor = descriptor;
		ParameterUtil.ensureConverted(descriptor);
	}

	interface Images extends ClientBundle {

		@ClientBundle.Source(
				value = "com/google/gwt/user/client/ui/disclosurePanelClosed.png"
		)
		ImageResource expand();

		@ClientBundle.Source(
				value = "com/google/gwt/user/client/ui/disclosurePanelOpen.png"
		)
		ImageResource collapse();
	}

	private static final Images images = com.google.gwt.core.client.GWT.create(Images.class);
}
