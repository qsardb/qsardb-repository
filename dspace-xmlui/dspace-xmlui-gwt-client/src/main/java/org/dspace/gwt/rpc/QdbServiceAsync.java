package org.dspace.gwt.rpc;

import com.google.gwt.core.client.*;
import com.google.gwt.user.client.rpc.*;

import org.dspace.gwt.client.*;

public interface QdbServiceAsync {

	void loadModelTable(String handle, String id, AsyncCallback<ModelTable> callback) throws DSpaceException;

	public static final ServiceManager<QdbServiceAsync> MANAGER = new ServiceManager<QdbServiceAsync>("/xmlui/rpc/service"){

		@Override
		protected Object createObject(){
			return GWT.create(QdbService.class);
		}
	};
}