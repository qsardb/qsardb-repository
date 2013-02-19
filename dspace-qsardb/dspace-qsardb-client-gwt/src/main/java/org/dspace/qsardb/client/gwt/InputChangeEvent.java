package org.dspace.qsardb.client.gwt;

import java.util.*;

import com.google.gwt.event.shared.*;

public class InputChangeEvent extends GwtEvent<InputChangeEventHandler> {

	private Map<String, String> values = null;


	public InputChangeEvent(Map<String, String> values){
		setValues(values);
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

	private void setValues(Map<String, String> values){
		this.values = values;
	}

	public static final Type<InputChangeEventHandler> TYPE = new Type<InputChangeEventHandler>();
}