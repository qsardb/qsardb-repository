/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.qsardb.client.gwt;

import com.google.gwt.core.client.GWT;
import java.util.Map;
import org.dspace.qsardb.rpc.gwt.PredictorRequest;
import org.dspace.qsardb.rpc.gwt.PredictorResponse;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.MethodCallback;

public class PredictorClient {

	private static PredictionService service;

	private static PredictionService getInstance() {
		if (service != null) {
			return  service;
		}

		String contextPath = Application.getInstance().getContextPath();
		Defaults.setServiceRoot(contextPath);
		service = GWT.create(PredictionService.class);
		return service;
	}

	public static void predict(PredictorRequest request, MethodCallback<PredictorResponse> callback) {
		QdbPredictor predictor = (QdbPredictor)Application.getInstance();
		String[] a = predictor.getHandle().split("/");
		getInstance().predict(a[0], a[1], predictor.getModelId(), request, callback);
	}
}
