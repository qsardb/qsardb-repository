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
import org.dspace.qsardb.rpc.gwt.PropertyColumn;
import org.dspace.qsardb.rpc.gwt.QdbTable;

public class ModelInputPanel extends Composite {

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

		DescriptorValueChangeEvent.Handler changeHandler = new DescriptorValueChangeEvent.Handler() {
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

	public Map<String, String> getDescriptorValues() {
		Map<String, String> values = new LinkedHashMap<>();

		for (DescriptorInputComponent descriptorInput : descriptorInputList) {
			values.put(descriptorInput.getId(), String.valueOf(descriptorInput.getValue()));
		}

		return values;
	}

	public void setDescriptorValues(Map<String, String> values, boolean fromArchive) {
		for (DescriptorInputComponent descriptorInput : descriptorInputList) {
			String did = descriptorInput.getId();

			String value = values.get(did);
			if (value == null) {
				continue;
			}

			descriptorInput.setValue(value);
			if (fromArchive) {
				descriptorInput.setDescriptorSource("This value is from the original model");
			}
		}
	}

	public void setDescriptorApplications(Map<String, String> apps) {
		if (apps == null || apps.isEmpty()) {
			return;
		}

		for (DescriptorInputComponent descriptorInput : descriptorInputList) {
			String did = descriptorInput.getId();
			String app = apps.getOrDefault(did, "N/A");
			descriptorInput.setDescriptorSource("This value is calculated with "+app);
		}
	}
}
