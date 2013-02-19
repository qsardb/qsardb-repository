package org.dspace.client.gwt;

import com.google.gwt.user.client.ui.*;

import org.dspace.rpc.gwt.*;

public class TableExplorerPanel extends ExplorerPanel {

	public TableExplorerPanel(ExplorerContext context, QdbTable table){
		super(context);

		Panel panel = new FlowPanel();

		panel.add(new Heading("Data table", 3));

		DataGridPanel gridPanel = new DataGridPanel(context, table);
		panel.add(gridPanel);

		initWidget(panel);
	}
}