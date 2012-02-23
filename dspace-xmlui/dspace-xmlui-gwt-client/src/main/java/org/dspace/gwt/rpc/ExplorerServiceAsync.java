package org.dspace.gwt.rpc;

import com.google.gwt.core.client.*;
import com.google.gwt.user.client.rpc.*;

import org.dspace.gwt.client.*;

public interface ExplorerServiceAsync {

	void loadModelTable(String handle, String id, AsyncCallback<ModelTable> callback) throws DSpaceException;

	public static final ServiceManager<ExplorerServiceAsync> MANAGER = new ServiceManager<ExplorerServiceAsync>("/xmlui/rpc/explorer"){

		@Override
		protected Object createObject(){
			return GWT.create(ExplorerService.class);
		}
	};
}