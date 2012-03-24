package org.dspace.gwt.client;

import com.google.gwt.event.shared.*;

public class InputChangeEvent extends GwtEvent<InputChangeEventHandler> {

	@Override
	public void dispatch(InputChangeEventHandler handler){
		handler.onValueChanged(this);
	}

	@Override
	public Type<InputChangeEventHandler> getAssociatedType(){
		return InputChangeEvent.TYPE;
	}

	public static final Type<InputChangeEventHandler> TYPE = new Type<InputChangeEventHandler>();
}