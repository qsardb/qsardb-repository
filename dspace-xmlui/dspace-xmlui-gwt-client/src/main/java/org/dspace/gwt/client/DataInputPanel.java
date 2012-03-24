package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class DataInputPanel extends Composite implements InputChangeEventHandler {

	private List<DescriptorInputPanel> descriptorPanels = new ArrayList<DescriptorInputPanel>();

	private Timer timer = new Timer(){

		@Override
		public void run(){
			evaluate();
		}
	};


	public DataInputPanel(QdbTable table){
		Panel panel = new VerticalPanel();

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		Map<String, Object> trainingValues = null;

		for(PredictionColumn prediction : predictions){

			if((prediction.getType()).equalsIgnoreCase("training")){
				trainingValues = prediction.getValues();
			}
		}

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for(DescriptorColumn descriptor : descriptors){
			DescriptorInputPanel descriptorPanel = new DescriptorInputPanel(descriptor);
			panel.add(descriptorPanel);

			this.descriptorPanels.add(descriptorPanel);

			Map<String, ?> trainingDescriptorValues = ParameterUtil.subset(trainingValues.keySet(), descriptor.getValues());

			Double mean = MathUtil.mean(trainingDescriptorValues.values());

			descriptorPanel.setValue(mean.toString());

			// Receive notifications about subsequent value changes
			descriptorPanel.addInputChangeEventHandler(this);
		}

		initWidget(panel);
	}

	@Override
	public void onValueChanged(InputChangeEvent event){
		this.timer.schedule(1000);
	}

	public void evaluate(){
		QdbPredictor predictor = (QdbPredictor)Application.getInstance();

		AsyncCallback<String> callback = new ServiceCallback<String>(){

			@Override
			public void onSuccess(String string){
				Window.alert(string);
			}
		};

		QdbServiceAsync service = (QdbServiceAsync.MANAGER).getInstance();

		try {
			service.evaluateModel(predictor.getHandle(), predictor.getModelId(), getDescriptorValues(), callback);
		} catch(DSpaceException de){
			Window.alert("Evaluation failed: " + de.getMessage());
		}
	}

	public Map<String, String> getDescriptorValues(){
		Map<String, String> values = new LinkedHashMap<String, String>();

		for(DescriptorInputPanel descriptorPanel : this.descriptorPanels){
			values.put(descriptorPanel.getId(), String.valueOf(descriptorPanel.getValue()));
		}

		return values;
	}

	public void setDescriptorValues(Map<String, String> values){

		for(DescriptorInputPanel descriptorPanel : this.descriptorPanels){
			String value = values.get(descriptorPanel.getId());

			if(value != null){
				descriptorPanel.setValue(value);
			}
		}
	}
}