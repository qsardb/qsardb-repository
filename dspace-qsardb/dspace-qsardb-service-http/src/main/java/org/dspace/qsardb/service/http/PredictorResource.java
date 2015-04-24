/*
 *  Copyright (c) 2014 University of Tartu
 */
package org.dspace.qsardb.service.http;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.dspace.content.Item;
import org.dspace.content.QdbCallable;
import org.dspace.content.QdbUtil;
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

		QdbCallable<String> cb = new QdbCallable<String>() {
			@Override
			public String call(Qdb qdb) throws Exception {
				Model model = qdb.getModel(modelId);
				if (model == null) {
					throw new NotFoundException("Model not found");
				}
				Map<String, String> params = parseDescriptors(qdb);

				String structure = getStructure(params);
				return PredictorUtil.evaluate(model, params, structure);
			}
		};

		org.dspace.core.Context context = QdbContext.getContext();
		Item item = ItemUtil.obtainItem(context, handle);
		if (item == null) {
			throw new NotFoundException(handle + " not found");
		}
		return QdbUtil.invokeInternal(context, item, cb);
	}

	private String getStructure(Map<String, String> params) {
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
	
	private Map<String, String> parseDescriptors(Qdb qdb){
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
