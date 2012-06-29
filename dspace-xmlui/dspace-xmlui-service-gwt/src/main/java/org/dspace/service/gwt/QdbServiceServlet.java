package org.dspace.service.gwt;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.Collection;

import org.qsardb.cargo.bodo.*;
import org.qsardb.cargo.map.*;
import org.qsardb.cargo.matrix.*;
import org.qsardb.cargo.structure.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

import org.dspace.content.*;
import org.dspace.content.QdbUtil;
import org.dspace.core.*;
import org.dspace.rpc.gwt.*;
import org.dspace.service.*;

public class QdbServiceServlet extends DSpaceRemoteServiceServlet implements QdbService {

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
		} catch(Exception e){
			throw formatException(e);
		}
	}

	@Override
	public Map<String, String> calculateModelDescriptors(final String handle, final String modelId, final String string) throws DSpaceException {
		Context context = getThreadLocalContext();

		try {
			Item item = obtainValidItem(context, handle);

			QdbCallable<Map<String, String>> callable = new QdbCallable<Map<String, String>>(){

				@Override
				public Map<String, String> call(Qdb qdb) throws Exception {
					return calculateModelDescriptors(qdb, modelId, string);
				}
			};

			return QdbUtil.invokeInternal(context, item, callable);
		} catch(Exception e){
			throw formatException(e);
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
		} catch(Exception e){
			throw formatException(e);
		}
	}

	private Item obtainValidItem(Context context, String handle) throws Exception {
		Item item = ItemUtil.obtainItem(context, handle);

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

		Map<String, String> leverages = new LinkedHashMap<String, String>();

		Map<String, String> mahalanobisDistances = new LinkedHashMap<String, String>();

		List<Prediction> predictions = new ArrayList<Prediction>();
		predictions.addAll((qdb.getPredictionRegistry()).getByModel(model));

		Comparator<Prediction> comparator = new Comparator<Prediction>(){

			@Override
			public int compare(Prediction left, Prediction right){
				return (left.getType()).compareTo(right.getType());
			}
		};
		Collections.sort(predictions, comparator);

		List<Compound> trainingCompounds = new ArrayList<Compound>();

		for(Prediction prediction : predictions){
			PredictionColumn column = loadPredictionColumn(prediction);
			column.setType(PredictionColumn.Type.valueOf((prediction.getType()).name()));

			Set<String> predictionKeys = (column.getValues()).keySet();

			if((prediction.getType()).equals(Prediction.Type.TRAINING)){
				trainingCompounds.addAll((qdb.getCompoundRegistry()).getAll(predictionKeys));
			}

			columns.add(column);

			keys.addAll(predictionKeys);

			leverages.putAll(loadLeverageValues(prediction));

			mahalanobisDistances.putAll(loadMahalanobisDistanceValues(prediction));
		}

		table.setKeys(keys);

		if(model.hasCargo(LeverageCargo.class) && leverages.size() > 0){
			LeverageCargo leverageCargo = model.getCargo(LeverageCargo.class);

			LeverageColumn column = new LeverageColumn();
			column.setCriticalValue(new BigDecimal(leverageCargo.getCriticalValue(trainingCompounds), QdbServiceServlet.context));

			column.setValues((Map)leverages);
			column.setLength(parseLength(leverages));

			columns.add(column);
		} // End if

		if(model.hasCargo(MahalanobisDistanceCargo.class) && mahalanobisDistances.size() > 0){
			MahalanobisDistanceCargo mahalanobisDistanceCargo = model.getCargo(MahalanobisDistanceCargo.class);

			MahalanobisDistanceColumn column = new MahalanobisDistanceColumn();
			column.setCriticalValue(new BigDecimal(mahalanobisDistanceCargo.getCriticalValue(trainingCompounds), QdbServiceServlet.context));

			column.setValues((Map)mahalanobisDistances);
			column.setLength(parseLength(mahalanobisDistances));

			columns.add(column);
		}

		Evaluator evaluator = QdbUtil.getEvaluator(model);

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

		AttributeCollector idValues = new AttributeCollector(){

			@Override
			public String collect(Compound compound){
				return compound.getId();
			}
		};

		AttributeCollector nameValues = new AttributeCollector(){

			@Override
			public String collect(Compound compound){
				return compound.getName();
			}
		};
		AttributeCollector casValues = new AttributeCollector(){

			@Override
			public String collect(Compound compound){
				return compound.getCas();
			}
		};
		AttributeCollector inChIValues = new AttributeCollector(){

			@Override
			public String collect(Compound compound){
				return compound.getInChI();
			}
		};

		CargoCollector smilesValues = new CargoCollector(ChemicalMimeData.DAYLIGHT_SMILES.getId());

		for(String key : keys){
			Compound compound = qdb.getCompound(key);

			idValues.add(compound);
			nameValues.add(compound);
			casValues.add(compound);
			inChIValues.add(compound);

			smilesValues.add(compound);
		}

		if(idValues.size() > 0){
			IdColumn column = new IdColumn();
			idValues.init(column);

			columns.add(column);
		} // End if

		if(nameValues.size() > 0){
			NameColumn column = new NameColumn();
			nameValues.init(column);

			columns.add(column);
		} // End if

		if(casValues.size() > 0){
			CasColumn column = new CasColumn();
			casValues.init(column);

			columns.add(column);
		} // End if

		if(inChIValues.size() > 0){
			InChIColumn column = new InChIColumn();
			inChIValues.init(column);

			columns.add(column);
		} // End if

		if(smilesValues.size() > 0){
			SmilesColumn column = new SmilesColumn();
			smilesValues.init(column);

			columns.add(column);
		}

		table.setColumns(columns);

		return table;
	}

	private Map<String, String> calculateModelDescriptors(Qdb qdb, String modelId, String string) throws Exception {
		Model model = qdb.getModel(modelId);
		if(model == null){
			throw new DSpaceException("Model \'" + modelId + "\' not found");
		}

		return PredictorUtil.calculateDescriptors(model, string);
	}

	private String evaluateModel(Qdb qdb, String modelId, Map<String, String> parameters) throws Exception {
		Model model = qdb.getModel(modelId);
		if(model == null){
			throw new DSpaceException("Model \'" + modelId + "\' not found");
		}

		return PredictorUtil.evaluate(model, parameters);
	}

	static
	private PredictionColumn loadPredictionColumn(Prediction prediction) throws IOException {
		PredictionColumn column = new PredictionColumn();
		column.setId(prediction.getId());
		column.setName(prediction.getName());

		Map<String, Object> values = loadValues(prediction);
		column.setValues(values);
		column.setLength(parseLength(values));

		return column;
	}

	static
	private PropertyColumn loadPropertyColumn(Property property, Collection<String> keys) throws IOException {
		PropertyColumn column = new PropertyColumn();
		column.setId(property.getId());
		column.setName(property.getName());

		Map<String, Object> values = loadValues(property, keys);
		column.setValues(values);
		column.setLength(parseLength(values));
		column.setFormat(parseFormat(values));

		return column;
	}

	static
	private DescriptorColumn loadDescriptorColumn(Descriptor descriptor, Collection<String> keys) throws IOException {
		DescriptorColumn column = new DescriptorColumn();
		column.setId(descriptor.getId());
		column.setName(descriptor.getName());

		Map<String, Object> values = truncateValues(loadValues(descriptor, keys));
		column.setValues(values);
		column.setLength(parseLength(values));
		column.setFormat(parseFormat(values));

		column.setCalculable(descriptor.hasCargo(BODOCargo.class));

		return column;
	}

	static
	private Map<String, Object> loadValues(Parameter<?, ?> parameter) throws IOException {
		ValuesCargo valuesCargo = parameter.getCargo(ValuesCargo.class);

		Map<String, Object> values = new LinkedHashMap<String, Object>(valuesCargo.loadStringMap());

		return values;
	}

	static
	private Map<String, Object> loadValues(Parameter<?, ?> parameter, Collection<String> keys) throws IOException {
		Map<String, Object> values = loadValues(parameter);

		if(keys != null && keys.size() > 0){
			(values.keySet()).retainAll(keys);
		}

		return values;
	}

	static
	private Map<String, Object> truncateValues(Map<String, Object> values){
		Collection<Map.Entry<String, Object>> entries = values.entrySet();

		for(Map.Entry<String, Object> entry : entries){
			Object value = entry.getValue();

			if(value instanceof String){
				String string = (String)value;

				// XXX
				if((string.startsWith("0.0") && string.endsWith("0")) && Double.parseDouble(string) == 0D){
					entry.setValue("0");
				}
			}
		}

		return values;
	}

	static
	private Map<String, String> loadLeverageValues(Prediction prediction) throws IOException {

		if(prediction.hasCargo(LeverageValuesCargo.class)){
			LeverageValuesCargo leverageValuesCargo = prediction.getCargo(LeverageValuesCargo.class);

			Map<String, String> leverageValues = new LinkedHashMap<String, String>(leverageValuesCargo.loadStringMap());

			return leverageValues;
		}

		return Collections.<String, String>emptyMap();
	}

	static
	private Map<String, String> loadMahalanobisDistanceValues(Prediction prediction) throws IOException {

		if(prediction.hasCargo(MahalanobisDistanceValuesCargo.class)){
			MahalanobisDistanceValuesCargo distanceValuesCargo = prediction.getCargo(MahalanobisDistanceValuesCargo.class);

			Map<String, String> distanceValues = new LinkedHashMap<String, String>(distanceValuesCargo.loadStringMap());

			return distanceValues;
		}

		return Collections.<String, String>emptyMap();
	}

	static
	private int parseLength(Map<String, ?> values){
		int length = 0;

		Collection<? extends Map.Entry<String, ?>> entries = values.entrySet();
		for(Map.Entry<String, ?> entry : entries){
			Object value = entry.getValue();

			if(value != null){
				String string = String.valueOf(value);

				length = Math.max(string.length(), length);
			}
		}

		return length;
	}

	static
	private String parseFormat(Map<String, ?> values){
		ScaleFrequencyMap map = new ScaleFrequencyMap();

		Collection<? extends Map.Entry<String, ?>> entries = values.entrySet();
		for(Map.Entry<String, ?> entry : entries){

			try {
				BigDecimal value = BigDecimalFormat.toBigDecimal(entry.getValue());

				map.add(value);
			} catch(Exception e){
				// Ignored
			}
		}

		int minCount = Math.max(5, map.getCountSum() / 10);

		return map.getPattern(minCount);
	}

	static
	abstract
	private class ValueCollector {

		private int length = 0;

		private Map<String, String> values = new LinkedHashMap<String, String>();


		abstract
		public String collect(Compound compound) throws Exception;

		public int size(){
			return this.values.size();
		}

		public void add(Compound compound) throws Exception {
			add(compound.getId(), collect(compound));
		}

		public void add(String key, String value){

			if(value == null){
				return;
			}

			this.length = Math.max(value.length(), this.length);

			this.values.put(key, value);
		}

		public <C extends QdbColumn<String>> C init(C column){
			column.setLength(getLength());
			column.setValues(getValues());

			return column;
		}

		public int getLength(){
			return this.length;
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
		public String collect(Compound compound) throws IOException {
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

	private static final MathContext context = new MathContext(8);
}