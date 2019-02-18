/*
 *  Copyright (c) 2014-2016 University of Tartu
 */
package org.dspace.qsardb.service.http;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import net.sf.blueobelisk.BODODescriptor;
import net.sf.blueobelisk.BODODescriptor.Implementation;
import org.dspace.content.Item;
import org.dspace.content.QdbCallable;
import org.dspace.content.QdbModelUtil;
import org.dspace.content.QdbParameterUtil;
import org.dspace.content.QdbUtil;
import org.dspace.qsardb.rpc.gwt.Analogue;
import org.dspace.qsardb.rpc.gwt.PredictorRequest;
import org.dspace.qsardb.rpc.gwt.PredictorResponse;
import org.dspace.qsardb.service.ApplicabilityDomain;
import org.dspace.qsardb.service.Distance;
import org.dspace.qsardb.service.ItemUtil;
import org.dspace.qsardb.service.PredictorUtil;
import org.dspace.qsardb.service.QdbContext;
import org.qsardb.cargo.bodo.BODOCargo;
import org.qsardb.cargo.structure.ChemicalMimeData;
import org.qsardb.evaluation.Evaluator;
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

	private static final CacheAD adCache = new CacheAD(500);

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{handle: \\d+/\\d+}/models/{modelId}")
	public String get(
			@PathParam("handle") String handle,
			@PathParam("modelId") final String modelId) throws Exception {
		try {
			PredictorResponse r = evaluate(handle, modelId, null);
			return r.getEquation();
		} catch (Exception ex) {
			throw new WebApplicationException("Evaluation failed", 400);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{handle: \\d+/\\d+}/models/{modelId}/info")
	public PredictorInfoResponse getPredictorInfo(
			@PathParam("handle") String handle,
			@PathParam("modelId") final String modelId) {
		try {
			return predictorInfo(handle, modelId);
		} catch (Exception ex) {
			throw new WebApplicationException("Error: "+ex.getMessage(), 400);
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
					app = "CDK " + org.openscience.cdk.CDK.getVersion();
				}

				r.getDescriptorApplications().put(descriptor.getId(), app);
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
				r.setEquation(result.getEquation());
				r.getPredictionValues().put(propertyId, result.getValue());

				if (req == null) { // it's a GET request
					return r;
				}

				ApplicabilityDomain ad = adCache.get(handle, model);
				ApplicabilityDomain.Result adResult = ad.estimate(params);
				r.setApplicabilityDomain(adResult.isWithinAD() ? "YES" : "NO");

				// round z-score values
				Map<String, Double> zScores = adResult.getDescriptorZScores();
				for (Map.Entry<String, Double> e: zScores.entrySet()) {
					double z = e.getValue();
					e.setValue(BigDecimal.valueOf(z).setScale(2, RoundingMode.HALF_UP).doubleValue());
				}
				r.setDescriptorZScores(zScores);

				Map<String, String> propValues = QdbParameterUtil.loadStringValues(model.getProperty());

				LinkedHashMap<String, String> predValues = new LinkedHashMap<>();
				for (Prediction p: qdb.getPredictionRegistry()) {
					predValues.putAll(QdbParameterUtil.loadStringValues(p));
				}

				List<Distance> analogues = adResult.getAnalogues();
				Integer limit = req.getLimitAnalogues();
				for (int i=0; i<Math.min(limit, analogues.size()); i++) {
					String cid = analogues.get(i).getCompoundId();
					Compound c = qdb.getCompound(cid);

					Analogue a = new Analogue(cid);
					a.setDistance(analogues.get(i).getDistance());
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

	private String getApplication(Descriptor descriptor) throws Exception {
		String app = descriptor.getApplication();

		if (app == null) {
			return "N/A";
		}

		if (app.trim().isEmpty() && descriptor.hasCargo(BODOCargo.class)) {
			BODODescriptor bodoDescriptor = descriptor.getCargo(BODOCargo.class).loadBodoDescriptor();
			for (Implementation impl: bodoDescriptor.getImplementations()) {
				if (impl.getTitle().contains("org.openscience.cdk")) {
					app = "CDK " + org.openscience.cdk.CDK.getVersion();
				}
				break;
			}
		}

		return app;
	}

	private PredictorInfoResponse predictorInfo(String handle, final String modelId) throws Exception {
		QdbCallable<PredictorInfoResponse> callback = new QdbCallable<PredictorInfoResponse>() {
			@Override
			public PredictorInfoResponse call(Qdb qdb) throws Exception {
				Model model = qdb.getModel(modelId);
				if (model == null) {
					throw new NotFoundException("Model not found: "+modelId);
				}

				Evaluator evaluator = QdbModelUtil.getEvaluator(model);

				evaluator.init();

				PredictorInfoResponse r = new PredictorInfoResponse();
				boolean acceptSMILES = true;
				try {
					List<Descriptor> descriptors = evaluator.getDescriptors();
					for (Descriptor d: descriptors) {
						r.getDescriptorNames().put(d.getId(), d.getName());
						r.getDescriptorApplications().put(d.getId(), getApplication(d));
						acceptSMILES &= d.hasCargo(BODOCargo.class);
					}
					r.setAcceptSMILES(acceptSMILES);
				} finally {
					evaluator.destroy();
				}

				return r;
			}
		};

		org.dspace.core.Context context = QdbContext.getContext();
		Item item = ItemUtil.obtainItem(context, handle);
		if (item == null || item.isWithdrawn()) {
			throw new NotFoundException(handle + " not found or unavailable");
		}

		PredictorInfoResponse r = QdbUtil.invokeInternal(context, item, callback);
		return r;
	}
}
