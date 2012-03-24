package org.dspace.gwt.service;

import java.io.*;
import java.util.*;
import java.util.Collection;

import org.qsardb.cargo.map.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.structure.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.gwt.rpc.*;

public class QdbServiceServlet extends ItemServiceServlet implements QdbService {

	@Override
	public ModelTable loadModelTable(final String handle, final String modelId) throws DSpaceException {
		Context context = getThreadLocalContext();

		try {
			Item item = obtainValidItem(context, handle);

			QdbCallable<ModelTable> callable = new QdbCallable<ModelTable>(){

				@Override
				public ModelTable call(Qdb qdb) throws Exception {
					return loadModelTable(qdb, modelId);
				}
			};

			return QdbUtil.invokeInternal(context, item, callable);
		} catch(DSpaceException de){
			throw de;
		} catch(Exception e){
			throw new DSpaceException(e.getMessage());
		}
	}

	@Override
	public String evaluateModel(final String handle, final String modelId, final Map<String, String> parameters) throws DSpaceException {
		Context context = getThreadLocalContext();

		try {
			Item item = obtainValidItem(context, handle);

			QdbCallable<String> callable = new QdbCallable<String>(){

				@Override
				public String call(Qdb qdb) throws Exception {
					return evaluateModel(qdb, modelId, parameters);
				}
			};

			return QdbUtil.invokeInternal(context, item, callable);
		} catch(DSpaceException de){
			throw de;
		} catch(Exception e){
			throw new DSpaceException(e.getMessage());
		}
	}

	private Item obtainValidItem(Context context, String handle) throws Exception {
		Item item = obtainItem(context, handle);
		if(item == null || item.isWithdrawn()){
			throw new DSpaceException("Handle \'" + handle + "\' not found or not valid");
		}

		return item;
	}

	private ModelTable loadModelTable(Qdb qdb, String modelId) throws Exception {
		ModelTable table = new ModelTable();

		Model model = qdb.getModel(modelId);
		if(model == null){
			throw new DSpaceException("Model \'" + modelId + "\' not found");
		}

		table.setId(model.getId());
		table.setName(model.getName());

		Set<String> keys = new LinkedHashSet<String>();

		List<QdbColumn<?>> columns = new ArrayList<QdbColumn<?>>();

		List<Prediction> predictions = new ArrayList<Prediction>();
		predictions.addAll((qdb.getPredictionRegistry()).getByModel(model));

		Comparator<Prediction> comparator = new Comparator<Prediction>(){

			@Override
			public int compare(Prediction left, Prediction right){
				return (left.getType()).compareTo(right.getType());
			}
		};
		Collections.sort(predictions, comparator);

		for(Prediction prediction : predictions){
			PredictionColumn column = loadPredictionColumn(prediction);
			column.setType((prediction.getType()).name());

			Map<String, ?> values = column.getValues();
			keys.addAll(values.keySet());

			columns.add(column);
		}

		table.setKeys(keys);

		Evaluator evaluator = getEvaluator(model);

		if(evaluator != null){
			evaluator.init();

			try {
				Property property = evaluator.getProperty();
				columns.add(loadPropertyColumn(property, keys));

				List<Descriptor> descriptors = evaluator.getDescriptors();
				for(Descriptor descriptor : descriptors){
					columns.add(loadDescriptorColumn(descriptor, keys));
				}
			} finally {
				evaluator.destroy();
			}
		} else

		{
			Property property = model.getProperty();
			columns.add(loadPropertyColumn(property, keys));
		}

		AttributeCollector nameValues = new AttributeCollector(){

			@Override
			public String getValue(Compound compound){
				return compound.getName();
			}
		};
		AttributeCollector casValues = new AttributeCollector(){

			@Override
			public String getValue(Compound compound){
				return compound.getCas();
			}
		};
		AttributeCollector inChIValues = new AttributeCollector(){

			@Override
			public String getValue(Compound compound){
				return compound.getInChI();
			}
		};

		CargoCollector smilesValues = new CargoCollector(ChemicalMimeData.DAYLIGHT_SMILES.getId());

		for(String key : keys){
			Compound compound = qdb.getCompound(key);

			nameValues.put(compound);
			casValues.put(compound);
			inChIValues.put(compound);

			smilesValues.put(compound);
		}

		if(nameValues.size() > 0){
			NameColumn column = new NameColumn();
			column.setValues(nameValues.getValues());

			columns.add(column);
		} // End if

		if(casValues.size() > 0){
			CasColumn column = new CasColumn();
			column.setValues(casValues.getValues());

			columns.add(column);
		} // End if

		if(inChIValues.size() > 0){
			InChIColumn column = new InChIColumn();
			column.setValues(inChIValues.getValues());

			columns.add(column);
		} // End if

		if(smilesValues.size() > 0){
			SmilesColumn column = new SmilesColumn();
			column.setValues(smilesValues.getValues());

			columns.add(column);
		}

		table.setColumns(columns);

		return table;
	}

	private String evaluateModel(Qdb qdb, String modelId, Map<String, String> parameters) throws Exception {
		Model model = qdb.getModel(modelId);
		if(model == null){
			throw new DSpaceException("Model \'" + modelId + "\' not found");
		}

		Evaluator evaluator = getEvaluator(model);
		if(evaluator != null){
			evaluator.init();

			try {
				List<Descriptor> descriptors = evaluator.getDescriptors();

				Evaluator.Result result = evaluator.evaluate(mapValues(descriptors, parameters));

				Object value = result.getValue();
				if(value != null){
					return String.valueOf(value);
				}

				return null;
			} finally {
				evaluator.destroy();
			}
		} else

		{
			throw new DSpaceException("Model \'" + modelId + "\' is not evaluatable");
		}
	}

	private PredictionColumn loadPredictionColumn(Prediction prediction) throws IOException {
		PredictionColumn column = new PredictionColumn();
		column.setId(prediction.getId());
		column.setName(prediction.getName());
		column.setValues(loadValues(prediction));

		return column;
	}

	private PropertyColumn loadPropertyColumn(Property property, Collection<String> keys) throws IOException {
		PropertyColumn column = new PropertyColumn();
		column.setId(property.getId());
		column.setName(property.getName());
		column.setValues(loadValues(property, keys));

		return column;
	}

	private DescriptorColumn loadDescriptorColumn(Descriptor descriptor, Collection<String> keys) throws IOException {
		DescriptorColumn column = new DescriptorColumn();
		column.setId(descriptor.getId());
		column.setName(descriptor.getName());
		column.setValues(loadValues(descriptor, keys));

		return column;
	}

	private Map<String, Object> loadValues(Parameter<?, ?> parameter) throws IOException {
		ValuesCargo valuesCargo = parameter.getCargo(ValuesCargo.class);

		Map<String, Object> values = new LinkedHashMap<String, Object>(valuesCargo.loadStringMap());

		return values;
	}

	private Map<String, Object> loadValues(Parameter<?, ?> parameter, Collection<String> keys) throws IOException {
		Map<String, Object> values = loadValues(parameter);

		if(keys != null && keys.size() > 0){
			(values.keySet()).retainAll(keys);
		}

		return values;
	}

	private <V> Map<Descriptor, V> mapValues(List<Descriptor> descriptors, Map<String, V> parameters){
		Map<Descriptor, V> values = new LinkedHashMap<Descriptor, V>();

		for(Descriptor descriptor : descriptors){
			values.put(descriptor, parameters.get(descriptor.getId()));
		}

		return values;
	}

	static
	private Evaluator getEvaluator(Model model) throws Exception {
		Qdb qdb = model.getQdb();

		if(model.hasCargo(PMMLCargo.class)){
			PMMLCargo pmmlCargo = model.getCargo(PMMLCargo.class);

			return new PMMLEvaluator(qdb, pmmlCargo.loadPmml());
		}

		return null;
	}

	static
	abstract
	private class ValueCollector {

		private Map<String, String> values = new LinkedHashMap<String, String>();


		abstract
		public String getValue(Compound compound) throws Exception;

		public int size(){
			return this.values.size();
		}

		public String get(String key){
			return this.values.get(key);
		}

		public void put(Compound compound) throws Exception {
			put(compound.getId(), getValue(compound));
		}

		public void put(String key, String value){

			if(value == null){
				return;
			}

			this.values.put(key, value);
		}

		public Map<String, String> getValues(){
			return this.values;
		}
	}

	static
	abstract
	private class AttributeCollector extends ValueCollector {
	}

	static
	private class CargoCollector extends ValueCollector {

		private String id = null;


		private CargoCollector(String id){
			setId(id);
		}

		@Override
		public String getValue(Compound compound) throws IOException {
			String id = getId();

			if(compound.hasCargo(id)){
				Cargo<?> cargo = compound.getCargo(id);

				return cargo.loadString();
			}

			return null;
		}

		public String getId(){
			return this.id;
		}

		private void setId(String id){
			this.id = id;
		}
	}
}