/*
 * Copyright (c) 2014 University of Tartu
 */
package org.dspace.qsardb.client.gwt;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dspace.qsardb.rpc.gwt.PredictionColumn;

class PredictionSelection extends Composite{

	private ArrayList<PredictionColumn> predictions = new ArrayList<PredictionColumn>();
	private ArrayList<CheckBox> checkBoxes = new ArrayList<CheckBox>();

	public PredictionSelection(List<PredictionColumn> predictions) {
		ClickHandler clickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireSelectionEvent();
			}
		};

		FlowPanel panel = new FlowPanel();
		panel.setStylePrimaryName("prediction-selection");
		panel.add(new InlineLabel("Predictions:"));
		for (PredictionColumn p: predictions){
			if(p.getValues().isEmpty()){
				continue;
			}

			FlowPanel predictionPanel = new FlowPanel();

			CheckBox checkBox = new CheckBox(p.getId()+": "+p.getName()); 
			checkBox.setValue(true);
			checkBox.addClickHandler(clickHandler);

			predictionPanel.add(checkBox);
			this.predictions.add(p);
			this.checkBoxes.add(checkBox);

			if (p.getDescription() != null) {
				predictionPanel.add(new DescriptionLabel(p));
			}

			panel.add(predictionPanel);
		}

		initWidget(panel);
	}

	public HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler){
		return addHandler(handler, SeriesDisplayEvent.TYPE);
	}

	private void fireSelectionEvent() {
		fireEvent(createSeriesDisplayEvent());
	}

	public SeriesDisplayEvent createSeriesDisplayEvent() {
		Map<PredictionColumn, Boolean> selection = new LinkedHashMap<PredictionColumn, Boolean>();
		for (int i=0; i<predictions.size(); i++) {
			selection.put(predictions.get(i), checkBoxes.get(i).getValue());
		}
		return new SeriesDisplayEvent(selection);
	}
}
