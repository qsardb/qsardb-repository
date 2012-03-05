package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class DataGridPanel extends Composite {

	public DataGridPanel(QdbTable table){
		DockPanel panel = new DockPanel();

		final
		CompoundDataProvider dataProvider = new CompoundDataProvider(CompoundDataProvider.format(table.getKeys()));

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
}