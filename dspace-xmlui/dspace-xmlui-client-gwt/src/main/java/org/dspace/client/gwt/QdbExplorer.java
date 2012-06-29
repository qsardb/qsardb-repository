package org.dspace.client.gwt;

import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.rpc.gwt.*;

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

		final
		SeriesPanel seriesPanel = new SeriesPanel(table);
		panel.add(seriesPanel);

		ExplorerContext context = new ExplorerContext(){

			@Override
			public HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler, boolean notify){
				HandlerRegistration result = seriesPanel.addSeriesDisplayEventHandler(handler);

				if(notify){
					handler.onSeriesVisibilityChanged(seriesPanel.createSeriesDisplayEvent());
				}

				return result;
			}
		};

		TableExplorerPanel tableExplorer = new TableExplorerPanel(context, table);
		panel.add(tableExplorer);

		PropertyExplorerPanel propertyExplorer = new PropertyExplorerPanel(context, table);
		panel.add(propertyExplorer);

		DescriptorExplorerPanel descriptorExplorer = new DescriptorExplorerPanel(context, table);
		panel.add(descriptorExplorer);

		if(table.hasColumn(LeverageColumn.class) || table.hasColumn(MahalanobisDistanceColumn.class)){
			ModelExplorerPanel modelExplorer = new ModelExplorerPanel(context, table);
			panel.add(modelExplorer);
		}

		return panel;
	}
}