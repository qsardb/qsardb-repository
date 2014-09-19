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

		PropertyColumn property = table.getColumn(PropertyColumn.class);
		if (property.isNumeric()) {
			panel.add(createPropertyPlotPanel(table));
			panel.add(createResidualErrorPlotPanel(table));
		} else {
			panel.add(createPropertyClassesPanel(table));
		}

		return panel;
	}

	private Widget createPropertyPlotPanel(final QdbTable table){
		PropertyColumn property = table.getColumn(PropertyColumn.class);
		String title = property.getId()+": "+property.getName();
		PlotPanel plotPanel = new PropertyPlotPanel(table);
		return createDisclosurePanel(title, plotPanel, true);
	}

	private Widget createResidualErrorPlotPanel(final QdbTable table){
		PlotPanel plotPanel = new ResidualErrorPlotPanel(table);
		return createDisclosurePanel("Residual error", plotPanel, false);
	}

	private Widget createPropertyClassesPanel(final QdbTable table){
		PropertyColumn property = table.getColumn(PropertyColumn.class);
		String title = property.getId()+": "+property.getName();
		PropertyClassesPanel classesPanel = new PropertyClassesPanel(table);
		return createDisclosurePanel(title, classesPanel, true);
	}

	private <W extends Widget & SeriesDisplayEventHandler> DisclosurePanel createDisclosurePanel(final String title, final W contentPanel, boolean startOpen) {
		DisclosurePanel panel = new DisclosurePanel();

		LazyHeader header = new LazyHeader(panel){
			@Override
			public Label createLeft(){
				return new Label(title);
			}
		};
		panel.setHeader(header);

		header.ensureWidget();

		LazyContent content = new LazyContent(panel){

			@Override
			public Widget createWidget(){
				getContext().addSeriesDisplayEventHandler(contentPanel, true);
				return contentPanel;
			}
		};
		panel.setContent(content);

		if(startOpen && !panel.isOpen()){
			panel.setOpen(true);
		}

		return panel;
	}
}
