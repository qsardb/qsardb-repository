/*
 *  Copyright (c) 2014 University of Tartu
 */
package org.dspace.qsardb.service.http;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.dspace.content.Item;
import org.dspace.content.QdbCallable;
import org.dspace.content.QdbUtil;
import org.dspace.qsardb.rpc.gwt.PredictorRequest;
import org.dspace.qsardb.rpc.gwt.PredictorResponse;
import org.dspace.qsardb.service.ItemUtil;
import org.dspace.qsardb.service.PredictorUtil;
import org.dspace.qsardb.service.QdbContext;
import org.qsardb.model.Model;
import org.qsardb.model.Qdb;

@Path("/predictor")
public class PredictorResource {

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{handle: \\d+/\\d+}/models/{modelId}")
	public String get(
			@PathParam("handle") String handle,
			@PathParam("modelId") final String modelId) throws Exception {
		try {
			PredictorResponse r = evaluate(handle, modelId, null);
			return r.getResult();
		} catch (Exception ex) {
			throw new WebApplicationException("Evaluation failed", 400);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{handle: \\d+/\\d+}/models/{modelId}")
	public PredictorResponse post(
			@PathParam("handle") String handle,
			@PathParam("modelId") final String modelId,
			PredictorRequest predictorRequest) throws Exception {
		try {
			PredictorResponse r = evaluate(handle, modelId, predictorRequest);
			return r;
		} catch (Exception ex) {
			throw new WebApplicationException("Evaluation failed", 400);
		}
	}

	private PredictorResponse evaluate(String handle, final String modelId, final PredictorRequest req) throws Exception {
		QdbCallable<PredictorResponse> cb = new QdbCallable<PredictorResponse>() {
			@Override
			public PredictorResponse call(Qdb qdb) throws Exception {
				Model model = qdb.getModel(modelId);
				if (model == null) {
					throw new NotFoundException("Model not found");
				}
				PredictorResponse r = new PredictorResponse();
				Map<String, String> params = getDescriptors(req, qdb);
				String structure = getStructure(req, params);
				if (structure == null) {
					r.setResult(PredictorUtil.evaluate(model, params));
				} else {
					r.setResult(PredictorUtil.evaluate(model, params, structure));
				}
				r.setParameters(params);

				return r;
			}
		};

		org.dspace.core.Context context = QdbContext.getContext();
		Item item = ItemUtil.obtainItem(context, handle);
		if (item == null || item.isWithdrawn()) {
			throw new NotFoundException(handle + " not found or unavailable");
		}
		PredictorResponse r = QdbUtil.invokeInternal(context, item, cb);
		return r;
	}

	private String getStructure(PredictorRequest postRequest, Map<String, String> params) {
		if (postRequest != null) {
			return postRequest.getStructure();
		}

		String query = uriInfo.getRequestUri().getQuery();
		if (query == null) {
			return null;
		}
		
		for (Map.Entry<String, String> pe: params.entrySet()){
			if (query.startsWith(pe.getKey())) {
				return null;
			}
		}
		
		int ampersand = query.indexOf("&");
		return ampersand != -1 ? query.substring(0, ampersand) : query;
	}
	
	private Map<String, String> getDescriptors(PredictorRequest postRequest, Qdb qdb){
		if (postRequest != null) {
			return new LinkedHashMap<String, String>(postRequest.getParameters());
		}

		Map<String, String> descs = new LinkedHashMap<String, String>();
		for (String did: uriInfo.getQueryParameters().keySet()) {
			String dv = uriInfo.getQueryParameters().getFirst(did);
			if (qdb.getDescriptor(did) == null || dv == null) {
				continue;
			}
			descs.put(did, dv);
		}
		return descs;
	}
}
