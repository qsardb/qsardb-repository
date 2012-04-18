package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class TableExplorerPanel extends ExplorerPanel {

	public TableExplorerPanel(ExplorerContext context, QdbTable table){
		super(context);

		Panel panel = new FlowPanel();

		panel.add(new Heading("Data table", 3));

		List<Compound> compounds = CompoundDataProvider.format(table.getKeys());

		CompoundDataProvider dataProvider = new CompoundDataProvider(compounds);

		context.addSeriesDisplayEventHandler(dataProvider, false);

		DataGridPanel gridPanel = new DataGridPanel(table, dataProvider);
		panel.add(gridPanel);

		initWidget(panel);
	}
}