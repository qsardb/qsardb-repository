package org.dspace.qsardb.client.gwt;

import com.google.gwt.core.shared.GWT;
import java.util.*;

import com.google.gwt.event.shared.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;

import org.dspace.qsardb.rpc.gwt.*;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

public class DataInputPanel extends Composite implements InputChangeEventHandler {

	private Timer timer = new Timer(){

		@Override
		public void run(){
			evaluate();
		}
	};

	private Map<String, String> values = null;

	interface Binder extends UiBinder<Widget, DataInputPanel> {}
	private static Binder binder = GWT.create(Binder.class);

	@UiField
	TabPanel tabPanel;

	@UiField(provided = true)
	CompoundSelectionPanel compoundSelectionPanel;

	@UiField(provided = true)
	CompoundInputPanel compoundInputPanel;
	
	@UiField(provided = true)
	ModelInputPanel modelInputPanel;

	public DataInputPanel(QdbTable table){
		initWidget(createUI(table));
		tabPanel.getTabBar().selectTab(0);
	}

	private Widget createUI(QdbTable table) {
		compoundSelectionPanel = new CompoundSelectionPanel(table);
		compoundInputPanel = new CompoundInputPanel(table);
		modelInputPanel = new ModelInputPanel(table);

		compoundSelectionPanel.addInputChangeEventHandler(this);
		compoundInputPanel.addInputChangeEventHandler(this);
		modelInputPanel.addInputChangeEventHandler(this);

		compoundSelectionPanel.addInputChangeEventHandler(modelInputPanel);
		compoundInputPanel.addInputChangeEventHandler(modelInputPanel);

		return binder.createAndBindUi(this);
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
		PredictorRequest request = new PredictorRequest(getValues());
		PredictorClient.predict(request, new MethodCallback<PredictorResponse>() {
			@Override
			public void onFailure(Method method, Throwable ex) {
				Window.alert("Evaluation failed: " + ex.getMessage());
			}

			@Override
			public void onSuccess(Method method, PredictorResponse response) {
				fireEvent(new EvaluationEvent(response));
			}
		});
	}

	public Map<String, String> getValues(){
		return this.values;
	}

	private void setValues(Map<String, String> values){
		this.values = values;
	}

	public void cleanCompoundData() {
		compoundSelectionPanel.suggestBox.setValue("", false);
		if (compoundInputPanel.textBox.isEnabled()) {
			compoundInputPanel.textBox.setValue("", false);
		}
	}
}