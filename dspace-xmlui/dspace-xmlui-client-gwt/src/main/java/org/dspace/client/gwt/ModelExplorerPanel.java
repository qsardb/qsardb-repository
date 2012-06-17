package org.dspace.client.gwt;

import com.google.gwt.user.client.ui.*;

import org.dspace.rpc.gwt.*;

public class ModelExplorerPanel extends ExplorerPanel {

	public ModelExplorerPanel(ExplorerContext context, QdbTable table){
		super(context);

		Panel panel = new FlowPanel();

		panel.add(new Heading("Applicability domain analysis", 3));

		panel.add(createModelPanel(table));

		initWidget(panel);
	}

	private Widget createModelPanel(QdbTable table){
		Panel panel = new VerticalPanel();

		panel.add(createWilliamsPlotPanel(table));

		return panel;
	}

	private Widget createWilliamsPlotPanel(final QdbTable table){
		DisclosurePanel panel = new DisclosurePanel();

		LazyHeader header = new LazyHeader(panel){

			@Override
			public Label createLeft(){
				return new Label("Williams plot");
			}
		};
		panel.setHeader(header);

		header.ensureWidget();

		LazyContent content = new LazyContent(panel){

			@Override
			public Widget createWidget(){
				PlotPanel plotPanel = new WilliamsPlotPanel(table);

				getContext().addSeriesDisplayEventHandler(plotPanel, true);

				return plotPanel;
			}
		};
		panel.setContent(content);

		return panel;
	}
}