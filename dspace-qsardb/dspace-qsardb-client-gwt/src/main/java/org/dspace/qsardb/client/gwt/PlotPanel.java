package org.dspace.qsardb.client.gwt;

import java.util.*;

import com.google.gwt.user.client.ui.*;

public class PlotPanel extends FlowPanel implements SeriesDisplayEventHandler {

	private List<QdbPlot> plots = new ArrayList<QdbPlot>();


	@Override
	public void add(Widget widget){
		addPlot(widget);

		if(widget instanceof ScatterPlot){
			ScatterPlot plot = (ScatterPlot)widget;

			SimplePanel panel = new SimplePanel();
			panel.setWidget(plot);

			plot.enableDetach(panel);

			super.add(panel);
		} else

		{
			super.add(widget);
		}
	}

	@Override
	public boolean remove(Widget widget){

		if(widget instanceof SimplePanel){
			SimplePanel panel = (SimplePanel)widget;

			removePlot(panel.getWidget());
		} else

		{
			removePlot(widget);
		}

		return super.remove(widget);
	}

	private void addPlot(Widget widget){

		if(widget instanceof QdbPlot){
			this.plots.add((QdbPlot)widget);
		}
	}

	private void removePlot(Widget widget){

		if(widget instanceof QdbPlot){
			this.plots.remove((QdbPlot)widget);
		}
	}

	@Override
	public void onSeriesVisibilityChanged(SeriesDisplayEvent event){

		for(QdbPlot plot : this.plots){
			plot.changeSeriesVisibility(event);
		}
	}
}