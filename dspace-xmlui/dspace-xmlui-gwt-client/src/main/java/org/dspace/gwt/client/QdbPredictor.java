package org.dspace.gwt.client;

import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class QdbPredictor extends QdbApplication {

	@Override
	public String getId(){
		return "aspect_artifactbrowser_QdbPredictor_div_main";
	}

	@Override
	public String getPath(){
		return "predictor";
	}

	@Override
	public Widget createWidget(ModelTable table){
		Panel panel = new FlowPanel();

		DataInputPanel inputPanel = new DataInputPanel(table);
		panel.add(inputPanel);

		// XXX
		panel.add(new HTML("&nbsp;"));

		DataOutputPanel outputPanel = new DataOutputPanel();
		panel.add(outputPanel);

		inputPanel.addEvaluationEventHandler(outputPanel);

		return panel;
	}
}