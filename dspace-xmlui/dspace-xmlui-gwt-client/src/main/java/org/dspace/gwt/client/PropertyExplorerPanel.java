package org.dspace.gwt.client;

import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class PropertyExplorerPanel extends ExplorerPanel {

	public PropertyExplorerPanel(ExplorerContext context, QdbTable table){
		super(context);

		Panel panel = new FlowPanel();

		panel.add(new Heading("Property analysis", 3));

		panel.add(createPropertyPanel(table));

		initWidget(panel);
	}

	private Widget createPropertyPanel(QdbTable table){
		Panel panel = new VerticalPanel();

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		panel.add(createPropertyPanel(table, property));

		return panel;
	}

	private Widget createPropertyPanel(final QdbTable table, final PropertyColumn property){
		DisclosurePanel panel = new DisclosurePanel();

		LazyHeader header = new LazyHeader(panel){

			@Override
			public Label createLeft(){
				return new Label(property.getName());
			}
		};
		panel.setHeader(header);

		header.ensureWidget();

		LazyContent content = new LazyContent(panel){

			@Override
			public Widget createWidget(){
				PlotPanel plotPanel = new PropertyPlotPanel(table, property);

				getContext().addSeriesDisplayEventHandler(plotPanel, true);

				return plotPanel;
			}
		};
		panel.setContent(content);

		if(!panel.isOpen()){
			panel.setOpen(true);
		}

		return panel;
	}
}