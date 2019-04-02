package org.dspace.qsardb.client.gwt;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dspace.qsardb.rpc.gwt.DescriptorColumn;
import org.dspace.qsardb.rpc.gwt.PredictionColumn;
import org.dspace.qsardb.rpc.gwt.PredictorResponse;
import org.dspace.qsardb.rpc.gwt.PropertyColumn;
import org.dspace.qsardb.rpc.gwt.QdbTable;

public class ModelInputPanel extends Composite implements InputChangeEventHandler {

	private final List<DescriptorInputComponent> descriptorInputList = new ArrayList<>();

	public ModelInputPanel(QdbTable table) {
		Panel panel = new VerticalPanel();

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);
		List<PredictionColumn> trainings = PredictionColumn.filter(predictions, PredictionColumn.Type.TRAINING);
		if (trainings.size() != 1) {
			throw new IllegalStateException();
		}
		PredictionColumn training = trainings.get(0);

		DescriptorValueChangeEventHandler changeHandler = new DescriptorValueChangeEventHandler() {
			@Override
			public void onDescriptorValueChanged(DescriptorValueChangeEvent event) {
				fireInputChangeEvent();
			}
		};

		panel.add(new HTML("<u>Descriptor input</u>:"));

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for (DescriptorColumn descriptor : descriptors) {
			DescriptorInputComponent descriptorInput = new DescriptorInputComponent(property, descriptor, training);
			descriptorInputList.add(descriptorInput);
			descriptorInput.addDescriptorValueChangeEventHandler(changeHandler);
			panel.add(descriptorInput);
		}

		initWidget(panel);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		fireInputChangeEvent();
	}

	public HandlerRegistration addInputChangeEventHandler(InputChangeEventHandler handler) {
		return addHandler(handler, InputChangeEvent.TYPE);
	}

	private void fireInputChangeEvent() {
		fireEvent(new InputChangeEvent(getDescriptorValues()));
	}

	@Override
	public void onInputChanged(InputChangeEvent event) {
		//need to turn it off atm to prevent clearing combobox
		for (DescriptorInputComponent dip : descriptorInputList) {
			dip.setEnableSlideEvents(false);
		}
		setDescriptorValues(event.getValues());
		for (DescriptorInputComponent dip : descriptorInputList) {
			dip.setEnableSlideEvents(true);
			if (dip.getSlider() != null) {
				dip.getSlider().normaliseMoreAndLess();
			}
		}

		if (event.getSource().getClass() == CompoundInputPanel.class) {
			final QdbPredictor predictor = (QdbPredictor)Application.getInstance();
			predictor.getDataInputPanel().compoundSelectionPanel.suggestBox.setValue("", false);

			for (DescriptorInputComponent dip : descriptorInputList) {
				dip.predictionSoftLabel.setText(getLabelText(dip.getDescriptor(), event.getResponse()));
			}
		} else if (event.getSource().getClass() == CompoundSelectionPanel.class) {
			for (DescriptorInputComponent dip : descriptorInputList) {
				dip.predictionSoftLabel.setText(getLabelText(dip.getDescriptor().getApplication()));
			}
		}
	}

	private String getLabelText(DescriptorColumn d, PredictorResponse response) {
		String result;
		String application = response.getDescriptorApplications().get(d.getId());
		if (application == null || application.trim().equals("")) {
			result = "Prediction value calculated with <N/A>";
		} else {
			result = "Current prediction is made with value calculated with " + application;
		}
		return result;
	}

	private String getLabelText(String application) {
		String result;
		if (application == null || application.trim().equals("")) {
			result = "Prediction value calculated with <N/A>";
		} else {
			result = "Current prediction is made with value calculated with " + application;
		}
		return result;
	}

	public Map<String, String> getDescriptorValues() {
		Map<String, String> values = new LinkedHashMap<>();

		for (DescriptorInputComponent descriptorInput : descriptorInputList) {
			values.put(descriptorInput.getId(), String.valueOf(descriptorInput.getValue()));
		}

		return values;
	}

	public void setDescriptorValues(Map<String, String> values) {
		for (DescriptorInputComponent descriptorInput : descriptorInputList) {
			String value = values.get(descriptorInput.getId());

			if (value != null) {
				descriptorInput.setValue(value);
			}
		}

	}
}
