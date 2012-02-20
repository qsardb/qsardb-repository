package org.dspace.gwt.rpc;

import com.google.gwt.user.client.rpc.*;

abstract
public class ServiceBroker<A> {

	private String path = null;

	private A instance = null;


	ServiceBroker(String path){
		setPath(path);
	}

	abstract
	protected Object createObject();

	public String getPath(){
		return this.path;
	}

	private void setPath(String path){
		this.path = path;
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	public A getInstance(){

		if(this.instance == null){
			Object object = createObject();

			ServiceDefTarget serviceDef = (ServiceDefTarget)object;
			serviceDef.setServiceEntryPoint(getPath());

			this.instance = (A)object;
		}

		return this.instance;
	}
}