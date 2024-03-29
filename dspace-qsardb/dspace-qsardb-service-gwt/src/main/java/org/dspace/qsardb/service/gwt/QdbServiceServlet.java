package org.dspace.qsardb.service.gwt;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.io.*;
import java.math.*;
import java.util.*;
import java.util.Collection;
import net.sf.blueobelisk.BODODescriptor.Implementation;

import org.qsardb.cargo.bodo.*;
import org.qsardb.cargo.map.*;
import org.qsardb.cargo.matrix.*;
import org.qsardb.cargo.structure.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.qsardb.rpc.gwt.*;
import org.dspace.qsardb.service.*;
import org.qsardb.cargo.ucum.UCUMCargo;

public class QdbServiceServlet extends RemoteServiceServlet implements QdbService {

	@Override
	public ModelTable loadModelTable(final String handle, final String modelId) throws DSpaceException {
		Context context = QdbContext.getContext();

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

	private DSpaceException formatException(Exception e){
		log(e.getMessage(), e);

		if(e instanceof DSpaceException){
			return (DSpaceException)e;
		}

		return new DSpaceException(e.getMessage());
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
		table.setDescription(Strings.emptyToNull(model.getDescription()));

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

		Property property = model.getProperty();
		PropertyColumn propertyColumn = loadPropertyColumn(property, keys);
		propertyColumn.setRegression(QdbModelUtil.isRegression(model));
		columns.add(propertyColumn);

		Evaluator evaluator = QdbModelUtil.getEvaluator(model);
		if (evaluator != null) {
			evaluator.init();
			try {
				List<Descriptor> descriptors = evaluator.getDescriptors();
				for(Descriptor descriptor : descriptors){
					columns.add(loadDescriptorColumn(descriptor, keys));
				}
			} finally {
				evaluator.destroy();
			}
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

		AttributeCollector descriptionValues = new AttributeCollector(){

			@Override
			public String collect(Compound compound){
				return Strings.emptyToNull(compound.getDescription());
			}
		};

		AttributeCollector labelsValues = new AttributeCollector(){

			@Override
			public String collect(Compound compound){
				return Joiner.on(", ").join(compound.getLabels());
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
			descriptionValues.add(compound);
			labelsValues.add(compound);
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

		if(descriptionValues.size() > 0) {
			DescriptionColumn column = new DescriptionColumn();
			descriptionValues.init(column);

			columns.add(column);
		}

		if (labelsValues.size() > 0) {
			LabelsColumn column = new LabelsColumn();
			labelsValues.init(column);

			columns.add(column);
		}

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

	static
	private PredictionColumn loadPredictionColumn(Prediction prediction) throws IOException {
		PredictionColumn column = new PredictionColumn();
		column.setId(prediction.getId());
		column.setName(prediction.getName());
		column.setDescription(Strings.emptyToNull(prediction.getDescription()));

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
		column.setDescription(Strings.emptyToNull(property.getDescription()));

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
		column.setApplication(descriptor.getApplication());

		//add units to column
		boolean hasUnits = descriptor.hasCargo(UCUMCargo.class);
		if (hasUnits) {
			String units;
			try {
				units = descriptor.getCargo(UCUMCargo.class).loadString();
			} catch (IOException ex) {
				units = "";
			}
			column.setUnits(units);
		}

		//add local descriptor calculation soft column
		boolean hasCargo = descriptor.hasCargo(BODOCargo.class);
		if (hasCargo) {
			String predictionApplication = "";

			BODOCargo bodoCargo = descriptor.getCargo(BODOCargo.class);

			List<Implementation> implementations = bodoCargo.loadBodoDescriptor().getImplementations();

			if (implementations.size() > 0) {
				predictionApplication = implementations.get(0).getTitle();
			}
			column.setPredictionApplication(predictionApplication);
		}

		column.setDescription(Strings.emptyToNull(descriptor.getDescription()));

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
