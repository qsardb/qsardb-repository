package org.dspace.gwt.client;

import com.google.gwt.user.client.ui.*;

public class DataOutputPanel extends Composite implements EvaluationEventHandler {

	private Label label = null;


	public DataOutputPanel(){
		Panel panel = new FlowPanel();

		this.label = new Label();
		panel.add(this.label);

		initWidget(panel);
	}

	@Override
	public void onEvaluate(EvaluationEvent event){
		this.label.setText(event.getResult());
	}
}