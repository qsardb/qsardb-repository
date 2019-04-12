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

	private final Timer timer = new Timer(){
		@Override
		public void run(){
			evaluate();
		}
	};

	private Map<String, String> values = null;
	private String structure = null;
	private String compoundId = null;

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

		return binder.createAndBindUi(this);
	}

	@Override
	public void onInputChanged(InputChangeEvent event){
		this.values = event.getValues();
		this.compoundId = event.getCompoundId();
		this.structure = event.getStructure();

		Object source = event.getSource();

		if (source == compoundSelectionPanel || source == modelInputPanel) {
			if (compoundInputPanel.textBox.isEnabled()) {
				compoundInputPanel.textBox.setValue("", false);
			}
		}
		if (source == compoundInputPanel || source == modelInputPanel) {
			compoundSelectionPanel.suggestBox.setValue("", false);
		}

		this.timer.schedule(1000);
	}

	public HandlerRegistration addEvaluationEventHandler(EvaluationEventHandler handler){
		return addHandler(handler, EvaluationEvent.TYPE);
	}

	public void evaluate(){
		PredictorRequest request;
		if (structure != null) {
			request = new PredictorRequest(structure);
		} else {
			request = new PredictorRequest(values);
		}
		PredictorClient.predict(request, new MethodCallback<PredictorResponse>() {
			@Override
			public void onFailure(Method method, Throwable ex) {
				Window.alert("Evaluation failed: " + ex.getMessage());
			}

			@Override
			public void onSuccess(Method method, PredictorResponse response) {
				modelInputPanel.setDescriptorValues(response.getDescriptorValues(), compoundId != null);
				modelInputPanel.setDescriptorApplications(response.getDescriptorApplications());
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
}