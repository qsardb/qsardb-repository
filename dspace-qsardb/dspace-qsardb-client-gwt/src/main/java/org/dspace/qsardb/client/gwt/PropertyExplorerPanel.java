package org.dspace.qsardb.client.gwt;

import com.google.gwt.user.client.ui.*;

import org.dspace.qsardb.rpc.gwt.*;

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

		panel.add(createPropertyPlotPanel(table));
		panel.add(createResidualErrorPlotPanel(table));

		return panel;
	}

	private Widget createPropertyPlotPanel(final QdbTable table){
		DisclosurePanel panel = new DisclosurePanel();

		final
		PropertyColumn property = table.getColumn(PropertyColumn.class);

		LazyHeader header = new LazyHeader(panel){

			@Override
			public Label createLeft(){
				return new Label(property.getId()+": "+property.getName());
			}
		};
		panel.setHeader(header);

		header.ensureWidget();

		LazyContent content = new LazyContent(panel){

			@Override
			public Widget createWidget(){
				PlotPanel plotPanel = new PropertyPlotPanel(table);

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

	private Widget createResidualErrorPlotPanel(final QdbTable table){
		DisclosurePanel panel = new DisclosurePanel();

		LazyHeader header = new LazyHeader(panel){

			@Override
			public Label createLeft(){
				return new Label("Residual error");
			}
		};
		panel.setHeader(header);

		header.ensureWidget();

		LazyContent content = new LazyContent(panel){

			@Override
			public Widget createWidget(){
				PlotPanel plotPanel = new ResidualErrorPlotPanel(table);

				getContext().addSeriesDisplayEventHandler(plotPanel, true);

				return plotPanel;
			}
		};
		panel.setContent(content);

		return panel;
	}
}