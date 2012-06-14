package org.dspace.client.gwt;

import java.util.*;

import com.google.gwt.user.client.ui.*;

public class PlotPanel extends FlowPanel implements SeriesDisplayEventHandler {

	@Override
	public void add(Widget widget){

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
	public void onSeriesVisibilityChanged(SeriesDisplayEvent event){

		for(Iterator<Widget> it = iterator(); it.hasNext(); ){
			Widget widget = it.next();

			if(widget instanceof QdbPlot){
				QdbPlot plot = (QdbPlot)widget;

				plot.changeSeriesVisibility(event);
			}
		}
	}
}