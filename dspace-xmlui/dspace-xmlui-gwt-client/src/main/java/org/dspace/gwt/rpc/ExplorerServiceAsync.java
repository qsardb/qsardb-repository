package org.dspace.gwt.rpc;

import com.google.gwt.core.client.*;
import com.google.gwt.user.client.rpc.*;

public interface ExplorerServiceAsync {

	void run(AsyncCallback<String> callback);

	public static final ServiceBroker<ExplorerServiceAsync> BROKER = new ServiceBroker<ExplorerServiceAsync>("/xmlui/rpc/explorer"){

		@Override
		protected Object createObject(){
			return GWT.create(ExplorerService.class);
		}
	};
}