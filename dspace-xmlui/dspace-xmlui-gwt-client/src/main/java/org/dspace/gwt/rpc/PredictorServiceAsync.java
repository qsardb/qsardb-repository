package org.dspace.gwt.rpc;

import com.google.gwt.core.client.*;
import com.google.gwt.user.client.rpc.*;

public interface PredictorServiceAsync {

	void run(AsyncCallback<String> callback);

	public static final ServiceBroker<PredictorServiceAsync> BROKER = new ServiceBroker<PredictorServiceAsync>("/xmlui/rpc/predictor"){

		@Override
		protected Object createObject(){
			return GWT.create(PredictorService.class);
		}
	};
}