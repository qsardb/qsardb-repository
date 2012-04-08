package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.event.shared.*;

import org.dspace.gwt.rpc.*;

public class SeriesDisplayEvent extends GwtEvent<SeriesDisplayEventHandler> {

	private Map<PredictionColumn, Boolean> values = null;


	public SeriesDisplayEvent(Map<PredictionColumn, Boolean> values){
		setValues(values);
	}

	public Set<PredictionColumn> getValues(Boolean value){
		Set<PredictionColumn> result = new LinkedHashSet<PredictionColumn>();

		Collection<Map.Entry<PredictionColumn, Boolean>> entries = getValues().entrySet();
		for(Map.Entry<PredictionColumn, Boolean> entry : entries){

			if((entry.getValue()).equals(value)){
				result.add(entry.getKey());
			}
		}

		return result;
	}

	public Map<PredictionColumn, Boolean> getValues(){
		return this.values;
	}

	private void setValues(Map<PredictionColumn, Boolean> values){
		this.values = values;
	}

	@Override
	public void dispatch(SeriesDisplayEventHandler handler){
		handler.onVisibilityChanged(this);
	}

	@Override
	public Type<SeriesDisplayEventHandler> getAssociatedType(){
		return SeriesDisplayEvent.TYPE;
	}

	public static final Type<SeriesDisplayEventHandler> TYPE = new Type<SeriesDisplayEventHandler>();
}