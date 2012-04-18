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

		TableExplorerPanel tableExplorer = new TableExplorerPanel(table);
		panel.add(tableExplorer);

		seriesPanel.addSeriesDisplayEventHandler(tableExplorer);

		PropertyExplorerPanel propertyExplorer = new PropertyExplorerPanel(table);
		panel.add(propertyExplorer);

		seriesPanel.addSeriesDisplayEventHandler(propertyExplorer);

		DescriptorExplorerPanel descriptorExplorer = new DescriptorExplorerPanel(table);
		panel.add(descriptorExplorer);

		seriesPanel.addSeriesDisplayEventHandler(descriptorExplorer);

		return panel;
	}
}