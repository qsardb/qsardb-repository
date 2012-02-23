package org.dspace.gwt.rpc;

import com.google.gwt.core.client.*;
import com.google.gwt.user.client.rpc.*;

import org.dspace.gwt.client.*;

public interface PredictorServiceAsync {

	void run(AsyncCallback<String> callback);

	public static final ServiceManager<PredictorServiceAsync> MANAGER = new ServiceManager<PredictorServiceAsync>("/xmlui/rpc/predictor"){

		@Override
		protected Object createObject(){
			return GWT.create(PredictorService.class);
		}
	};
}