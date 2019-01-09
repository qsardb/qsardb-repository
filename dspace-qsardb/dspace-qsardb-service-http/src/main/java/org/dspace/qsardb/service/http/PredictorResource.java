/*
 *  Copyright (c) 2014-2016 University of Tartu
 */
package org.dspace.qsardb.service.http;

import java.io.IOException;
import java.util.ArrayList;
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
import net.sf.blueobelisk.BODODescriptor;
import org.dspace.content.Item;
import org.dspace.content.QdbCallable;
import org.dspace.content.QdbParameterUtil;
import org.dspace.content.QdbUtil;
import org.dspace.qsardb.rpc.gwt.Analogue;
import org.dspace.qsardb.rpc.gwt.PredictorRequest;
import org.dspace.qsardb.rpc.gwt.PredictorResponse;
import org.dspace.qsardb.service.ItemUtil;
import org.dspace.qsardb.service.PredictorUtil;
import org.dspace.qsardb.service.QdbContext;
import org.dspace.qsardb.service.Distance;
import org.dspace.qsardb.service.DistanceCalculator;
import org.qsardb.cargo.bodo.BODOCargo;
import org.qsardb.cargo.structure.ChemicalMimeData;
import org.qsardb.model.Compound;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;
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

	protected PredictorResponse calculateDescriptors(String handle, Model model, String structure) throws Exception {
		Map<Descriptor, String> result = PredictorUtil.calculateDescriptors(model, structure);

		LinkedHashMap<String, String> params = new LinkedHashMap<>();

		PredictorResponse r = new PredictorResponse();
		for(Descriptor descriptor : result.keySet()) {
			params.put(descriptor.getId(), result.get(descriptor));

			if(!descriptor.hasCargo(BODOCargo.class)){
				continue;
			}

			BODODescriptor bodoDescriptor = descriptor.getCargo(BODOCargo.class).loadBodoDescriptor();

			if (bodoDescriptor.getImplementations().size() > 0) {
				String app = bodoDescriptor.getImplementations().get(0).getTitle();
				if (app.contains("org.openscience.cdk")) {
					app = "CDK, version " + org.openscience.cdk.CDK.getVersion();
				}

				r.getImplementations().put(descriptor.getId(), app);
			}
		}

		r.setDescriptorValues(params);

		return r;
	}

	private PredictorResponse evaluate(final String handle, final String modelId, final PredictorRequest req) throws Exception {
		QdbCallable<PredictorResponse> cb = new QdbCallable<PredictorResponse>() {
			@Override
			public PredictorResponse call(Qdb qdb) throws Exception {
				Model model = qdb.getModel(modelId);
				if (model == null) {
					throw new NotFoundException("Model not found");
				}

				Map<String, String> params = getDescriptors(req, qdb);

				String structure = getStructure(req, params);

				PredictorResponse r;
				if (structure != null) {
					r = calculateDescriptors(handle, model, structure);
					params = r.getDescriptorValues();
				} else {
					r = new PredictorResponse();
					r.setDescriptorValues(params);
				}

				String propertyId = model.getProperty().getId();

				String units = QdbParameterUtil.loadUnits(model.getProperty());
				r.getPredictionUnits().put(propertyId, units);

				PredictorUtil.Result result = PredictorUtil.evaluate(model, params);
				r.setResult(result.getEquation());
				r.getPredictionValues().put(propertyId, result.getValue());

				if (req == null) { // it's a GET request
					return r;
				}

				DistanceCalculator calc = new DistanceCalculator(model);
				ArrayList<Distance> dists = calc.calculateDistances(params);

				Map<String, String> propValues = QdbParameterUtil.loadStringValues(model.getProperty());

				LinkedHashMap<String, String> predValues = new LinkedHashMap<>();
				for (Prediction p: qdb.getPredictionRegistry()) {
					predValues.putAll(QdbParameterUtil.loadStringValues(p));
				}

				for (int i=0; i<Math.min(5, dists.size()); i++) {
					String cid = dists.get(i).getCompoundId();
					Compound c = qdb.getCompound(cid);

					Analogue a = new Analogue(cid);
					a.setDistance(dists.get(i).getDistance());
					a.setName(c.getName());
					a.setCas(c.getCas());
					a.setSmiles(loadSmiles(c));

					a.getPropertyValues().put(propertyId, propValues.get(cid));
					a.getPredictionValues().put(propertyId, predValues.get(cid));

					r.getAnalogues().add(a);
				}

				return r;
			}

			private String loadSmiles(Compound c) throws IOException {
				String cargoID = ChemicalMimeData.DAYLIGHT_SMILES.getId();
				if (c.hasCargo(cargoID)) {
					return c.getCargo(cargoID).loadString();
				}

				return "";
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
			return postRequest.getDescriptorValues();
		}

		Map<String, String> descs = new LinkedHashMap<>();
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
