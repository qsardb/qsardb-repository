package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class DescriptorExplorerPanel extends ExplorerPanel {

	public DescriptorExplorerPanel(ExplorerContext context, QdbTable table){
		super(context);

		Panel panel = new FlowPanel();

		panel.add(new Heading("Descriptor analysis", 3));

		panel.add(createDescriptorListPanel(table));

		initWidget(panel);
	}

	private Widget createDescriptorListPanel(QdbTable table){
		Panel panel = new VerticalPanel();

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for(DescriptorColumn descriptor : descriptors){
			panel.add(createDescriptorPanel(table, property, descriptor));
		}

		return panel;
	}

	private Widget createDescriptorPanel(final QdbTable table, final PropertyColumn property, final DescriptorColumn descriptor){
		DisclosurePanel panel = new DisclosurePanel();

		LazyHeader header = new LazyHeader(panel){

			@Override
			public Label createLeft(){
				return new Label(descriptor.getName());
			}
		};
		panel.setHeader(header);

		header.ensureWidget();

		LazyContent content = new LazyContent(panel){

			@Override
			public Widget createWidget(){
				PlotPanel plotPanel = new DescriptorPlotPanel(table, property, descriptor);

				getContext().addSeriesDisplayEventHandler(plotPanel, true);

				return plotPanel;
			}
		};
		panel.setContent(content);

		return panel;
	}
}