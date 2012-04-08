package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class SeriesPanel extends Composite {

	public SeriesPanel(QdbTable table){
		Panel panel = new FlowPanel();

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		final
		SeriesListBox seriesList = new SeriesListBox(predictions);
		panel.add(seriesList);

		ChangeHandler changeHandler = new ChangeHandler(){

			@Override
			public void onChange(ChangeEvent event){
				Map<PredictionColumn, Boolean> value = seriesList.getSelectedValue();

				fireEvent(new SeriesDisplayEvent(value));
			}
		};
		seriesList.addChangeHandler(changeHandler);

		initWidget(panel);
	}

	public HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler){
		return addHandler(handler, SeriesDisplayEvent.TYPE);
	}
}