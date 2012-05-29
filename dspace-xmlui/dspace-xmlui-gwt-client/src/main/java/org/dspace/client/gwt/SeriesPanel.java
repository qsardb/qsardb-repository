package org.dspace.client.gwt;

import java.util.*;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.rpc.gwt.*;

public class SeriesPanel extends Composite {

	private SeriesListBox seriesList = null;


	public SeriesPanel(QdbTable table){
		Panel panel = new FlowPanel();

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		this.seriesList = new SeriesListBox(predictions);
		panel.add(this.seriesList);

		ChangeHandler changeHandler = new ChangeHandler(){

			@Override
			public void onChange(ChangeEvent event){
				fireSeriesDisplayEvent();
			}
		};
		this.seriesList.addChangeHandler(changeHandler);

		initWidget(panel);
	}

	private void fireSeriesDisplayEvent(){
		fireEvent(createSeriesDisplayEvent());
	}

	protected SeriesDisplayEvent createSeriesDisplayEvent(){
		Map<PredictionColumn, Boolean> value = this.seriesList.getSelectedValue();

		return new SeriesDisplayEvent(value);
	}

	public HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler){
		return addHandler(handler, SeriesDisplayEvent.TYPE);
	}
}