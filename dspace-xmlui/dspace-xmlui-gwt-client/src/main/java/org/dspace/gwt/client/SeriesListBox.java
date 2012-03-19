package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class SeriesListBox extends ListBox {

	private List<Map<PredictionColumn, Boolean>> values = new ArrayList<Map<PredictionColumn, Boolean>>();


	public SeriesListBox(){
		setVisibleItemCount(1);
	}

	public void addItem(String label, Map<PredictionColumn, Boolean> value){

		// XXX
		if(value.isEmpty()){
			return;
		}

		addItem(label);

		this.values.add(value);
	}

	public Map<PredictionColumn, Boolean> getSelectedValue(){
		int index = getSelectedIndex();

		return this.values.get(index);
	}

	static
	public Map<PredictionColumn, Boolean> createValue(Collection<PredictionColumn> predictions){
		return createValue(predictions, null);
	}

	static
	public Map<PredictionColumn, Boolean> createValue(Collection<PredictionColumn> predictions, String type){
		Map<PredictionColumn, Boolean> result = new LinkedHashMap<PredictionColumn, Boolean>();

		for(PredictionColumn prediction : predictions){

			if(type == null || (prediction.getType()).equalsIgnoreCase(type)){
				result.put(prediction, Boolean.TRUE);
			}
		}

		return result;
	}
}