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

		panel.add(new DataGridPanel(table));

		// XXX
		panel.add(new HTML("&nbsp;"));

		panel.add(new DataAnalysisPanel(table));

		return panel;
	}
}