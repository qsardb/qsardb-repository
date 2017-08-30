/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.qsardb.client.gwt;

import javax.ws.rs.Encoded;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.dspace.qsardb.rpc.gwt.PredictorRequest;
import org.dspace.qsardb.rpc.gwt.PredictorResponse;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

public interface PredictionService extends RestService {
	@POST
	@Path("service/predictor/{prefix}/{suffix}/models/{modelId}")
	public void predict(
			@PathParam("prefix") String handlePrefix,
			@PathParam("suffix") String handleSuffix,
			@PathParam("modelId") String modelId,
			PredictorRequest request, 
			MethodCallback<PredictorResponse> callback);
}
