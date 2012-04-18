package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;
import com.google.gwt.view.client.*;

import org.dspace.gwt.rpc.*;

public class DataGridPanel extends Composite {

	public DataGridPanel(QdbTable table, final ListDataProvider<Compound> dataProvider){
		DockPanel panel = new DockPanel();

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

		createGridPager(panel, dataGrid, DockPanel.NORTH);
		createGridPager(panel, dataGrid, DockPanel.SOUTH);

		initWidget(panel);
	}

	private void createGridPager(DockPanel panel, DataGrid<Compound> dataGrid, DockLayoutConstant position){
		CompoundSimplePager dataGridPager = new CompoundSimplePager();
		dataGridPager.setDisplay(dataGrid);

		panel.add(dataGridPager, position);

		// XXX
		panel.setCellWidth(dataGridPager, "100%");
		panel.setCellHeight(dataGridPager, "20px");
	}
}