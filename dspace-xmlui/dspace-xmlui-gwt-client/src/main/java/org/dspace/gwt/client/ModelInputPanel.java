package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class ModelInputPanel extends Composite implements InputChangeEventHandler {

	public ModelInputPanel(QdbTable table){
		Panel panel = new VerticalPanel();

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		Map<String, Object> trainingValues = null;

		for(PredictionColumn prediction : predictions){

			if((prediction.getType()).equalsIgnoreCase("training")){
				trainingValues = prediction.getValues();
			}
		}

		DescriptorValueChangeEventHandler changeHandler = new DescriptorValueChangeEventHandler(){

			@Override
			public void onDescriptorValueChanged(DescriptorValueChangeEvent event){
				fireEvent(new InputChangeEvent(getDescriptorValues()));
			}
		};

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for(DescriptorColumn descriptor : descriptors){
			DescriptorInputPanel descriptorPanel = new DescriptorInputPanel(descriptor);
			panel.add(descriptorPanel);

			Map<String, ?> trainingDescriptorValues = ParameterUtil.subset(trainingValues.keySet(), descriptor.getValues());

			Double mean = MathUtil.mean(trainingDescriptorValues.values());

			descriptorPanel.setValue(mean.toString());

			// Receive notifications about subsequent value changes
			descriptorPanel.addDescriptorValueChangeEventHandler(changeHandler);
		}

		initWidget(panel);
	}

	public HandlerRegistration addInputChangeEventHandler(InputChangeEventHandler handler){
		return addHandler(handler, InputChangeEvent.TYPE);
	}

	@Override
	public void onInputChanged(InputChangeEvent event){
		setDescriptorValues(event.getValues());
	}

	public Map<String, String> getDescriptorValues(){
		List<DescriptorInputPanel> descriptorPanels = getDescriptorPanels();

		Map<String, String> values = new LinkedHashMap<String, String>();

		for(DescriptorInputPanel descriptorPanel : descriptorPanels){
			values.put(descriptorPanel.getId(), String.valueOf(descriptorPanel.getValue()));
		}

		return values;
	}

	public void setDescriptorValues(Map<String, String> values){
		List<DescriptorInputPanel> descriptorPanels = getDescriptorPanels();

		for(DescriptorInputPanel descriptorPanel : descriptorPanels){
			String value = values.get(descriptorPanel.getId());

			if(value != null){
				descriptorPanel.setValue(value);
			}
		}
	}

	private List<DescriptorInputPanel> getDescriptorPanels(){
		List<DescriptorInputPanel> result = new ArrayList<DescriptorInputPanel>();

		ComplexPanel panel = (ComplexPanel)getWidget();

		for(int i = 0; i < panel.getWidgetCount(); i++){
			Widget widget = panel.getWidget(i);

			if(widget instanceof DescriptorInputPanel){
				result.add((DescriptorInputPanel)widget);
			}
		}

		return result;
	}
}