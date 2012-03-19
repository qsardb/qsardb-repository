package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class DataAnalysisPanel extends Composite {

	public DataAnalysisPanel(QdbTable table){
		DockPanel panel = new DockPanel();

		panel.add(createSeriesPanel(table), DockPanel.NORTH);
		panel.add(createAnalysisPanel(table), DockPanel.CENTER);

		initWidget(panel);
	}

	private Widget createSeriesPanel(QdbTable table){
		Panel panel = new FlowPanel();

		final
		SeriesListBox seriesList = new SeriesListBox();
		panel.add(seriesList);

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		if(predictions.size() > 1){
			seriesList.addItem("All predictions", SeriesListBox.createValue(predictions));
		}

		seriesList.addItem("Training prediction", SeriesListBox.createValue(predictions, "training"));

		if(predictions.size() > 1){
			seriesList.addItem("Validation prediction(s)", SeriesListBox.createValue(predictions, "validation"));
			seriesList.addItem("Testing prediction(s)", SeriesListBox.createValue(predictions, "testing"));
		}

		ChangeHandler changeHandler = new ChangeHandler(){

			@Override
			public void onChange(ChangeEvent event){
				Map<PredictionColumn, Boolean> value = seriesList.getSelectedValue();

				fireEvent(new SeriesDisplayEvent(value));
			}
		};
		seriesList.addChangeHandler(changeHandler);

		return panel;
	}

	private Widget createAnalysisPanel(QdbTable table){
		Panel panel = new FlowPanel();

		panel.add(createPropertyPanel(table));
		panel.add(createDescriptorPanel(table));

		return panel;
	}

	private Widget createPropertyPanel(QdbTable table){
		Panel panel = new FlowPanel();
		panel.add(new HTML("<h4>Property analysis</h4>"));

		PlotGrid grid = new PropertyPlotGrid(table);
		addSeriesDisplayEventHandler(grid);

		panel.add(grid);

		return panel;
	}

	private Widget createDescriptorPanel(QdbTable table){
		Panel panel = new FlowPanel();
		panel.add(new HTML("<h4>Descriptor analysis</h4>")); // XXX

		PlotGrid grid = new DescriptorPlotGrid(table);
		addSeriesDisplayEventHandler(grid);

		panel.add(grid);

		return panel;
	}

	public HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler){
		return addHandler(handler, SeriesDisplayEvent.TYPE);
	}
}