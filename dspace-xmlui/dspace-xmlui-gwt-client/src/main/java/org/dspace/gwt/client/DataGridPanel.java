package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.event.shared.*;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class DataGridPanel extends Composite implements SeriesDisplayEventHandler {

	public DataGridPanel(QdbTable table){
		DockPanel panel = new DockPanel();

		List<Compound> compounds = CompoundDataProvider.format(table.getKeys());

		final
		CompoundDataProvider dataProvider = new CompoundDataProvider(compounds);

		addSeriesDisplayEventHandler(dataProvider);

		final
		CompoundDataGrid dataGrid = new CompoundDataGrid(table);
		dataProvider.addDataDisplay(dataGrid);

		ResizeLayoutPanel dataGridPanel = new ResizeLayoutPanel();
		dataGridPanel.setWidget(dataGrid);

		// XXX
		dataGridPanel.setWidth("100%");
		dataGridPanel.setHeight("580px");

		panel.add(dataGridPanel, DockPanel.CENTER);

		ColumnSortEvent.Handler sortHandler = new ColumnSortEvent.Handler(){

			@Override
			public void onColumnSort(ColumnSortEvent event){
				Comparator<Compound> comparator = dataGrid.getComparator(event.getColumn());

				if(comparator == null){
					return;
				} // End if

				if(!event.isSortAscending()){
					comparator = Collections.reverseOrder(comparator);
				}

				Collections.sort(dataProvider.getList(), comparator);
			}
		};
		dataGrid.addColumnSortHandler(sortHandler);

		final
		CompoundSimplePager dataGridPager = new CompoundSimplePager();
		dataGridPager.setDisplay(dataGrid);

		panel.add(dataGridPager, DockPanel.SOUTH);

		// XXX
		panel.setCellWidth(dataGridPager, "100%");
		panel.setCellHeight(dataGridPager, "20px");

		initWidget(panel);
	}

	@Override
	public void onVisibilityChanged(SeriesDisplayEvent event){
		fireEvent(event);
	}

	public HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler){
		return addHandler(handler, SeriesDisplayEvent.TYPE);
	}
}