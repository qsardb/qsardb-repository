package org.dspace.client.gwt;

import java.util.*;

import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.rpc.gwt.*;

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

		CompoundInputPanel compoundPanel = new CompoundInputPanel(table);
		compoundPanel.addInputChangeEventHandler(this);

		panel.add(compoundPanel);

		// XXX
		panel.add(new HTML("&nbsp;"));

		ModelInputPanel modelPanel = new ModelInputPanel(table);
		modelPanel.addInputChangeEventHandler(this);

		panel.add(modelPanel);

		compoundPanel.addInputChangeEventHandler(modelPanel);

		initWidget(panel);
	}

	@Override
	public void onInputChanged(InputChangeEvent event){
		setValues(event.getValues());

		this.timer.schedule(1000);
	}

	public HandlerRegistration addEvaluationEventHandler(EvaluationEventHandler handler){
		return addHandler(handler, EvaluationEvent.TYPE);
	}

	public void evaluate(){
		QdbPredictor predictor = (QdbPredictor)Application.getInstance();

		AsyncCallback<String> callback = new ServiceCallback<String>(){

			@Override
			public void onSuccess(String string){
				fireEvent(new EvaluationEvent(string));
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