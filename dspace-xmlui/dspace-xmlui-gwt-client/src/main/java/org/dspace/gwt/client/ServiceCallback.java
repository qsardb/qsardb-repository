package org.dspace.gwt.client;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;

abstract
public class ServiceCallback<R> implements AsyncCallback<R> {

	@Override
	public void onFailure(Throwable throwable){
		Window.alert(throwable.toString());
	}
}