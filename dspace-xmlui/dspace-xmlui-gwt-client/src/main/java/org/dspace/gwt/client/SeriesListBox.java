package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class SeriesListBox extends ListBox {

	private List<Map<PredictionColumn, Boolean>> values = new ArrayList<Map<PredictionColumn, Boolean>>();


	public SeriesListBox(List<PredictionColumn> predictions){
		setVisibleItemCount(1);

		if(predictions.size() > 1){
			addItem("All predictions", createValue(predictions));
		}

		addItem("Training prediction", createValue(predictions, PredictionColumn.Type.TRAINING));

		if(predictions.size() > 1){
			addItem("Validation prediction(s)", createValue(predictions, PredictionColumn.Type.VALIDATION));
			addItem("Testing prediction(s)", createValue(predictions, PredictionColumn.Type.TESTING));
		}
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
	private Map<PredictionColumn, Boolean> createValue(Collection<PredictionColumn> predictions){
		return createValue(predictions, null);
	}

	static
	private Map<PredictionColumn, Boolean> createValue(Collection<PredictionColumn> predictions, PredictionColumn.Type type){
		Map<PredictionColumn, Boolean> result = new LinkedHashMap<PredictionColumn, Boolean>();

		for(PredictionColumn prediction : predictions){

			if(type == null || (prediction.getType()).equals(type)){
				result.put(prediction, Boolean.TRUE);
			}
		}

		return result;
	}
}