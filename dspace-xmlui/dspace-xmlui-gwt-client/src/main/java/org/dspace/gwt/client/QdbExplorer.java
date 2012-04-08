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

		panel.add(createHeader("Data table"));

		DataGridPanel gridPanel = new DataGridPanel(table);
		seriesPanel.addSeriesDisplayEventHandler(gridPanel);

		panel.add(gridPanel);

		panel.add(createHeader("Property analysis"));

		PlotGrid propertyGrid = new PropertyPlotGrid(table);
		seriesPanel.addSeriesDisplayEventHandler(propertyGrid);

		panel.add(new ScrollPanel(propertyGrid));

		panel.add(createHeader("Descriptor analysis"));

		PlotGrid descriptorGrid = new DescriptorPlotGrid(table);
		seriesPanel.addSeriesDisplayEventHandler(descriptorGrid);

		panel.add(new ScrollPanel(descriptorGrid));

		return panel;
	}

	private Widget createHeader(String string){
		return new HTML("<h4>" + string + "</h4>");
	}
}