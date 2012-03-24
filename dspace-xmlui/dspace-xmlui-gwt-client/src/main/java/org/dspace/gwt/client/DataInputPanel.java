package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class DataInputPanel extends Composite implements InputChangeEventHandler {

	private Timer timer = new Timer(){

		@Override
		public void run(){
			evaluate();
		}
	};

	private Map<String, String> values = null;


	public DataInputPanel(QdbTable table){
		Panel panel = new FlowPanel();

		ModelInputPanel modelPanel = new ModelInputPanel(table);
		modelPanel.addInputChangeEventHandler(this);

		panel.add(modelPanel);

		initWidget(panel);
	}

	@Override
	public void onInputChanged(InputChangeEvent event){
		setValues(event.getValues());

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
			service.evaluateModel(predictor.getHandle(), predictor.getModelId(), getValues(), callback);
		} catch(DSpaceException de){
			Window.alert("Evaluation failed: " + de.getMessage());
		}
	}

	public Map<String, String> getValues(){
		return this.values;
	}

	private void setValues(Map<String, String> values){
		this.values = values;
	}
}