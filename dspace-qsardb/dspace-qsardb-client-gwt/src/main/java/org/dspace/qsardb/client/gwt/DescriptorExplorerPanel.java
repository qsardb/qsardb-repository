package org.dspace.qsardb.client.gwt;

import java.util.*;

import com.google.gwt.user.client.ui.*;

import org.dspace.qsardb.rpc.gwt.*;

public class DescriptorExplorerPanel extends ExplorerPanel {

	public DescriptorExplorerPanel(ExplorerContext context, QdbTable table){
		super(context);

		Panel panel = new FlowPanel();

		panel.add(new Heading("Descriptor analysis", 3));

		panel.add(createDescriptorListPanel(table));

		initWidget(panel);
	}

	private Widget createDescriptorListPanel(QdbTable table){
		Panel panel = new FlowPanel();

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for(DescriptorColumn descriptor : descriptors){
			panel.add(createDescriptorPlotPanel(table, descriptor));
		}

		return panel;
	}

	private Widget createDescriptorPlotPanel(final QdbTable table, final DescriptorColumn descriptor){
		DisclosurePanel panel = new DisclosurePanel();

		LazyHeader header = new LazyHeader(panel){

			@Override
			public Widget createLeft(){
				FlowPanel headerPanel = new FlowPanel();
				headerPanel.add(new InlineLabel(descriptor.getId()+": "+descriptor.getName()));
				if (descriptor.getDescription() != null){
					headerPanel.add(new DescriptionLabel(new DescriptionTooltip(descriptor)));
				}
				return headerPanel;
			}
		};
		panel.setHeader(header);

		header.ensureWidget();

		LazyContent content = new LazyContent(panel){

			@Override
			public Widget createWidget(){
				PlotPanel plotPanel = new DescriptorPlotPanel(table, descriptor);

				getContext().addSeriesDisplayEventHandler(plotPanel, true);

				return plotPanel;
			}
		};
		panel.setContent(content);

		return panel;
	}
}