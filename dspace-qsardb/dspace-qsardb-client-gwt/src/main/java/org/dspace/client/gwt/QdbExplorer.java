package org.dspace.client.gwt;

import java.util.*;

import org.dspace.rpc.gwt.*;

import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

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

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		final
		SeriesMenuBar seriesMenu = new SeriesMenuBar(predictions);

		ExplorerContext context = new ExplorerContext(){

			@Override
			public HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler, boolean notify){
				HandlerRegistration result = seriesMenu.addSeriesDisplayEventHandler(handler);

				if(notify){
					handler.onSeriesVisibilityChanged(seriesMenu.createSeriesDisplayEvent());
				}

				return result;
			}
		};

		MenuBar menu = new MenuBar();
		menu.addItem("Series", seriesMenu);

		panel.add(menu);

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