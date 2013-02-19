package org.dspace.rpc.gwt;

import java.util.*;

import com.google.gwt.core.client.*;
import com.google.gwt.user.client.rpc.*;

import org.dspace.client.gwt.*;

public interface QdbServiceAsync {

	void loadModelTable(String handle, String modelId, AsyncCallback<ModelTable> callback) throws DSpaceException;

	void calculateModelDescriptors(String handle, String modelId, String string, AsyncCallback<Map<String, String>> callback) throws DSpaceException;

	void evaluateModel(String handle, String modelId, Map<String, String> parameters, AsyncCallback<String> callback) throws DSpaceException;

	public static final ServiceManager<QdbServiceAsync> MANAGER = new ServiceManager<QdbServiceAsync>("/service/gwt"){

		@Override
		protected Object createObject(){
			return GWT.create(QdbService.class);
		}
	};
}