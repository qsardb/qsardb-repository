package org.dspace.qsardb.client.gwt;

import java.util.*;

import com.google.gwt.event.shared.*;
import org.dspace.qsardb.rpc.gwt.PredictorResponse;

public class InputChangeEvent extends GwtEvent<InputChangeEventHandler> {

	private Map<String, String> values = null;
	private PredictorResponse response = null;


	public InputChangeEvent(Map<String, String> values){
		setValues(values);
	}

	public InputChangeEvent(PredictorResponse response){
		this.response = response;
		setValues(response.getParameters());
	}

	@Override
	public void dispatch(InputChangeEventHandler handler){
		handler.onInputChanged(this);
	}

	@Override
	public Type<InputChangeEventHandler> getAssociatedType(){
		return InputChangeEvent.TYPE;
	}

	public Map<String, String> getValues(){
		return this.values;
	}

	public PredictorResponse getResponse(){
		return response;
	}

	private void setValues(Map<String, String> values){
		this.values = values;
	}

	public static final Type<InputChangeEventHandler> TYPE = new Type<InputChangeEventHandler>();
}
