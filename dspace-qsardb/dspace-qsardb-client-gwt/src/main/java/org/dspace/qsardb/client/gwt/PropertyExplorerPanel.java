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
		PlotPanel plotPanel = new PropertyPlotPanel(table);
		Widget header = createPropertyHeaderWidget(table);
		return createDisclosurePanel(header, plotPanel, true);
	}

	private Widget createResidualErrorPlotPanel(final QdbTable table){
		PlotPanel plotPanel = new ResidualErrorPlotPanel(table);
		Widget header = createResidualHeaderWidget(table);
		return createDisclosurePanel(header, plotPanel, false);
	}

	private Widget createPropertyClassesPanel(final QdbTable table){
		PropertyClassesPanel classesPanel = new PropertyClassesPanel(table);
		Widget header = createPropertyHeaderWidget(table);
		return createDisclosurePanel(header, classesPanel, true);
	}

	private Widget createPropertyHeaderWidget(final QdbTable table){
		PropertyColumn property = table.getColumn(PropertyColumn.class);
		String title = property.getId()+": "+property.getName();
		FlowPanel titlePanel = new FlowPanel();
		titlePanel.add(new InlineLabel(title));
		if (property.getDescription() != null) {
			titlePanel.add(new DescriptionLabel(property));
		}
		return titlePanel;
	}

	private Widget createResidualHeaderWidget(final QdbTable table){
		return new Label("Residual error");
	}

	private <W extends Widget & SeriesDisplayEventHandler> DisclosurePanel createDisclosurePanel(final Widget headerWidget, final W contentPanel, boolean startOpen) {
		DisclosurePanel panel = new DisclosurePanel();

		LazyHeader header = new LazyHeader(panel){
			@Override
			public Widget createLeft(){
				return headerWidget;
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
