package org.dspace.gwt.service;

import java.io.*;
import java.util.*;
import java.util.Collection;

import org.qsardb.cargo.map.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.gwt.rpc.*;

public class ExplorerServiceServlet extends ItemServiceServlet implements ExplorerService {

	@Override
	public ModelTable loadModelTable(final String handle, final String id) throws DSpaceException {
		Context context = getThreadLocalContext();

		try {
			Item item = obtainItem(context, handle);
			if(item == null || item.isWithdrawn()){
				throw new DSpaceException("Handle \'" + handle + "\' not found or not valid");
			}

			QdbCallable<ModelTable> callable = new QdbCallable<ModelTable>(){

				@Override
				public ModelTable call(Qdb qdb) throws Exception {
					return loadModelTable(qdb, id);
				}
			};

			return QdbUtil.invoke(context, item, callable);
		} catch(DSpaceException de){
			throw de;
		} catch(Exception e){
			throw new DSpaceException(e.getMessage());
		}
	}

	private ModelTable loadModelTable(Qdb qdb, String id) throws Exception {
		ModelTable table = new ModelTable();

		Model model = qdb.getModel(id);
		if(model == null){
			throw new DSpaceException("Model \'" + id + "\' not found");
		}

		table.setId(model.getId());
		table.setName(model.getName());

		Set<String> keys = new LinkedHashSet<String>();

		List<QdbColumn<?>> columns = new ArrayList<QdbColumn<?>>();

		Collection<Prediction> predictions = (qdb.getPredictionRegistry()).getByModel(model);
		for(Prediction prediction : predictions){
			PredictionColumn column = loadPredictionColumn(prediction);

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

		ValueCollector nameValues = new ValueCollector();
		ValueCollector casValues = new ValueCollector();
		ValueCollector inChIValues = new ValueCollector();

		for(String key : keys){
			Compound compound = qdb.getCompound(key);

			nameValues.put(key, compound.getName());
			casValues.put(key, compound.getCas());
			inChIValues.put(key, compound.getInChI());
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
		}

		table.setColumns(columns);

		return table;
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
	private class ValueCollector {

		private Map<String, String> values = new LinkedHashMap<String, String>();


		public int size(){
			return this.values.size();
		}

		public String get(String key){
			return this.values.get(key);
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
}