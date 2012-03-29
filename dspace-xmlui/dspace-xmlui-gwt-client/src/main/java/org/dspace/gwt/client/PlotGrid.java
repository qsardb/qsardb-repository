package org.dspace.gwt.client;

import com.google.gwt.user.client.ui.*;

public class PlotGrid extends Grid implements SeriesDisplayEventHandler {

	public QdbPlot getPlot(int row, int column){
		return (QdbPlot)getWidget(row, column);
	}

	public void setPlot(int row, int column, QdbPlot plot){
		setWidget(row, column, plot);
	}

	@Override
	public void onVisibilityChanged(SeriesDisplayEvent event){
		int rows = getRowCount();
		int columns = getColumnCount();

		for(int row = 0; row < rows; row++){

			for(int column = 0; column < columns; column++){
				Widget widget = getWidget(row, column);

				if(widget instanceof QdbPlot){
					QdbPlot plot = (QdbPlot)widget;

					plot.changeSeriesVisibility(event.getValues());
				}
			}
		}
	}
}