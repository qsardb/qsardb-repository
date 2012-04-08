package org.dspace.gwt.client;

import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class QdbExplorer extends QdbApplication {

	@Override
	public String getId(){
		return "aspect_artifactbrowser_QdbExplorer_div_main";
	}

	@Override
	public String getPath(){
		return "explorer";
	}

	@Override
	public Widget createWidget(ModelTable table){
		Panel panel = new FlowPanel();

		SeriesPanel seriesPanel = new SeriesPanel(table);
		panel.add(seriesPanel);

		panel.add(new Heading("Data table", 3));

		DataGridPanel gridPanel = new DataGridPanel(table);
		seriesPanel.addSeriesDisplayEventHandler(gridPanel);

		panel.add(gridPanel);

		panel.add(new Heading("Property analysis", 3));

		PlotGrid propertyGrid = new PropertyPlotGrid(table);
		seriesPanel.addSeriesDisplayEventHandler(propertyGrid);

		panel.add(new ScrollPanel(propertyGrid));

		panel.add(new Heading("Descriptor analysis", 3));

		PlotGrid descriptorGrid = new DescriptorPlotGrid(table);
		seriesPanel.addSeriesDisplayEventHandler(descriptorGrid);

		panel.add(new ScrollPanel(descriptorGrid));

		return panel;
	}
}