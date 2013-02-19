package org.dspace.qsardb.client.gwt;

import com.google.gwt.event.shared.*;

import org.dspace.qsardb.rpc.gwt.*;

public class DescriptorValueChangeEvent extends GwtEvent<DescriptorValueChangeEventHandler> {

	private DescriptorColumn descriptor = null;


	public DescriptorValueChangeEvent(DescriptorColumn descriptor){
		setDescriptor(descriptor);
	}

	@Override
	public void dispatch(DescriptorValueChangeEventHandler handler){
		handler.onDescriptorValueChanged(this);
	}

	@Override
	public Type<DescriptorValueChangeEventHandler> getAssociatedType(){
		return DescriptorValueChangeEvent.TYPE;
	}

	public DescriptorColumn getDescriptor(){
		return this.descriptor;
	}

	private void setDescriptor(DescriptorColumn descriptor){
		this.descriptor = descriptor;
	}

	public static final Type<DescriptorValueChangeEventHandler> TYPE = new Type<DescriptorValueChangeEventHandler>();
}