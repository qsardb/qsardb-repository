package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class QdbExplorer extends Application {

	@Override
	public String getId(){
		return "aspect_artifactbrowser_QdbExplorer_div_main";
	}

	@Override
	public void onModuleLoad(){
		setWidget(new Label("Loading.."));

		ExplorerServiceAsync service = (ExplorerServiceAsync.MANAGER).getInstance();

		AsyncCallback<ModelTable> callback = new ServiceCallback<ModelTable>(){

			@Override
			public void onSuccess(ModelTable table){
				convertTable(table);

				setWidget(createWidget(table));
			}
		};

		try {
			service.loadModelTable(match("explorer/(.*)", 1, Window.Location.getPath()), match("(.*)", 1, Window.Location.getParameter("model")), callback);
		} catch(DSpaceException de){
			setWidget(new Label("Loading failed: " + de.getMessage()));
		}
	}

	private Widget createWidget(ModelTable table){
		DockPanel panel = new DockPanel();

		final
		CompoundDataProvider dataProvider = new CompoundDataProvider(CompoundDataProvider.format(table.getKeys()));

		final
		CompoundDataGrid dataGrid = new CompoundDataGrid(table.getColumns());
		dataProvider.addDataDisplay(dataGrid);

		ResizeLayoutPanel dataGridPanel = new ResizeLayoutPanel();
		dataGridPanel.setWidget(dataGrid);

		// XXX
		dataGridPanel.setWidth("100%");
		dataGridPanel.setHeight("480px");

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
		SimplePager dataGridPager = new SimplePager();
		dataGridPager.setDisplay(dataGrid);

		panel.add(dataGridPager, DockPanel.SOUTH);

		// XXX
		panel.setCellWidth(dataGridPager, "100%");
		panel.setCellHeight(dataGridPager, "20px");

		return panel;
	}

	static
	private void convertTable(QdbTable table){
		List<QdbColumn<?>> columns = table.getColumns();

		for(QdbColumn<?> column : columns){

			if(column instanceof ParameterColumn){
				convertParameterColumn((ParameterColumn)column);
			}
		}
	}

	static
	private void convertParameterColumn(ParameterColumn column){
		Map<String, Object> values = column.getValues();

		Collection<Map.Entry<String, Object>> entries = values.entrySet();
		for(Map.Entry<String, Object> entry : entries){
			String key = entry.getKey();
			Object value = entry.getValue();

			try {
				if(value instanceof String){
					String string = (String)value;

					entry.setValue(new BigDecimal(string));
				}
			} catch(NumberFormatException nfe){
				// Ignored
			}
		}
	}
}