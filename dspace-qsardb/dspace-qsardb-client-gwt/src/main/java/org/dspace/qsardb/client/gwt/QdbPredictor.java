package org.dspace.qsardb.client.gwt;

import com.google.gwt.user.client.ui.*;

import org.dspace.qsardb.rpc.gwt.*;

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

		panel.add(new Heading("Model input", 3));

		DataInputPanel inputPanel = new DataInputPanel(table);
		panel.add(inputPanel);

		panel.add(new Heading("Model output", 3));

		DataOutputPanel outputPanel = new DataOutputPanel();
		panel.add(outputPanel);

		inputPanel.addEvaluationEventHandler(outputPanel);

		panel.add(new Heading("Similar predictions", 3));

		CompoundDistancePanel similarityPanel = new CompoundDistancePanel(table);
		panel.add(similarityPanel);

		inputPanel.addEvaluationEventHandler(similarityPanel);

		return panel;
	}
}