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

		if(table.hasColumn(LeverageColumn.class)){
			panel.add(createWilliamsPlotPanel(table, LeverageColumn.class));
		} // End if

		if(table.hasColumn(MahalanobisDistanceColumn.class)){
			panel.add(createWilliamsPlotPanel(table, MahalanobisDistanceColumn.class));
		} // End if

		if(table.hasColumn(LeverageColumn.class)){
			panel.add(createGramaticaPlotPanel(table, LeverageColumn.class));
		} // End if

		if(table.hasColumn(MahalanobisDistanceColumn.class)){
			panel.add(createGramaticaPlotPanel(table, MahalanobisDistanceColumn.class));
		}

		return panel;
	}

	private Widget createWilliamsPlotPanel(final QdbTable table, final Class<? extends DistanceColumn> clazz){
		DisclosurePanel panel = new DisclosurePanel();

		LazyHeader header = new LazyHeader(panel){

			@Override
			public Label createLeft(){
				return new Label(getHeader("Williams plot", table, clazz));
			}
		};
		panel.setHeader(header);

		header.ensureWidget();

		LazyContent content = new LazyContent(panel){

			@Override
			public Widget createWidget(){
				PlotPanel plotPanel = new WilliamsPlotPanel(table, clazz);

				getContext().addSeriesDisplayEventHandler(plotPanel, true);

				return plotPanel;
			}
		};
		panel.setContent(content);

		return panel;
	}

	private Widget createGramaticaPlotPanel(final QdbTable table, final Class<? extends DistanceColumn> clazz){
		DisclosurePanel panel = new DisclosurePanel();

		LazyHeader header = new LazyHeader(panel){

			@Override
			public Label createLeft(){
				return new Label(getHeader("Gramatica plot", table, clazz));
			}
		};
		panel.setHeader(header);

		header.ensureWidget();

		LazyContent content = new LazyContent(panel){

			@Override
			public Widget createWidget(){
				PlotPanel plotPanel = new GramaticaPlotPanel(table, clazz);

				getContext().addSeriesDisplayEventHandler(plotPanel, true);

				return plotPanel;
			}
		};
		panel.setContent(content);

		return panel;
	}

	static
	private String getHeader(String title, QdbTable table, Class<?> clazz){

		if(table.hasColumn(LeverageColumn.class) && table.hasColumn(MahalanobisDistanceColumn.class)){

			if((LeverageColumn.class).equals(clazz)){
				return title + " (Leverage)";
			} else

			if((MahalanobisDistanceColumn.class).equals(clazz)){
				return title + " (Mahalanobis distance)";
			}
		}

		return title;
	}
}