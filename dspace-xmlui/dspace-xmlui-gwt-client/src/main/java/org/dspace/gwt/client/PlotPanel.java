package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.client.ui.*;

public class PlotPanel extends FlowPanel implements SeriesDisplayEventHandler {

	@Override
	public void onVisibilityChanged(SeriesDisplayEvent event){

		for(Iterator<Widget> it = iterator(); it.hasNext(); ){
			Widget widget = it.next();

			if(widget instanceof QdbPlot){
				QdbPlot plot = (QdbPlot)widget;

				plot.changeSeriesVisibility(event);
			}
		}
	}
}