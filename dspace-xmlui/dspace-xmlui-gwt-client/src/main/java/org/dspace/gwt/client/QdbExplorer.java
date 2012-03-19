package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

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
		Panel panel = new FlowPanel();

		panel.add(new DataGridPanel(table));
		panel.add(new HTML("&nbsp;")); // XXX
		panel.add(new DataAnalysisPanel(table));

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

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);
		for(PredictionColumn prediction : predictions){
			prediction.setErrors(calculateErrors(prediction, property));
		}
	}

	static
	private void convertParameterColumn(ParameterColumn column){
		Map<String, Object> values = column.getValues();

		Collection<Map.Entry<String, Object>> entries = values.entrySet();
		for(Map.Entry<String, Object> entry : entries){
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

	static
	private Map<String, BigDecimal> calculateErrors(PredictionColumn prediction, PropertyColumn property){
		Map<String, BigDecimal> result = new LinkedHashMap<String, BigDecimal>();

		Set<String> ids = new LinkedHashSet<String>((prediction.getValues()).keySet());
		ids.retainAll((property.getValues()).keySet());

		for(String id : ids){
			Object left = prediction.getValue(id);
			Object right = property.getValue(id);

			if(left instanceof BigDecimal && right instanceof BigDecimal){
				BigDecimal error = ((BigDecimal)left).subtract((BigDecimal)right);

				result.put(id, error);
			}
		}

		return result;
	}
}