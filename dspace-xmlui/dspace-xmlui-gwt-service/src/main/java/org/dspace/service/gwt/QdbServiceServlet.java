package org.dspace.service.gwt;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.Collection;

import org.qsardb.cargo.bodo.*;
import org.qsardb.cargo.map.*;
import org.qsardb.cargo.structure.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

import net.sf.blueobelisk.*;
import net.sf.jniinchi.*;

import org.dspace.content.*;
import org.dspace.content.QdbUtil;
import org.dspace.core.*;
import org.dspace.rpc.gwt.*;
import org.dspace.service.*;
import org.openscience.cdk.*;
import org.openscience.cdk.exception.*;
import org.openscience.cdk.graph.*;
import org.openscience.cdk.inchi.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.qsar.*;
import org.openscience.cdk.qsar.result.*;
import org.openscience.cdk.smiles.*;

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
			column.setType(PredictionColumn.Type.valueOf((prediction.getType()).name()));

			Map<String, ?> values = column.getValues();
			keys.addAll(values.keySet());

			columns.add(column);
		}

		table.setKeys(keys);

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

		IAtomContainer molecule = parseMolecule(string);

		Evaluator evaluator = QdbUtil.getEvaluator(model);

		if(evaluator != null){
			evaluator.init();

			try {
				Map<String, String> values = new LinkedHashMap<String, String>();

				List<Descriptor> descriptors = evaluator.getDescriptors();
				for(Descriptor descriptor : descriptors){

					if(!descriptor.hasCargo(BODOCargo.class)){
						continue;
					}

					BODOCargo bodoCargo = descriptor.getCargo(BODOCargo.class);

					BODODescriptor bodoDescriptor = bodoCargo.loadBodoDescriptor();

					IDescriptor cdkDescriptor = BODOUtil.parse(bodoDescriptor);

					values.put(descriptor.getId(), calculateCdkDescriptor((IMolecularDescriptor)cdkDescriptor, molecule));
				}

				return values;
			} finally {
				evaluator.destroy();
			}
		} else

		{
			throw new DSpaceException("Model \'" + modelId + "\' is not evaluateable");
		}
	}

	private String evaluateModel(Qdb qdb, String modelId, Map<String, String> parameters) throws Exception {
		Model model = qdb.getModel(modelId);
		if(model == null){
			throw new DSpaceException("Model \'" + modelId + "\' not found");
		}

		Evaluator evaluator = QdbUtil.getEvaluator(model);

		if(evaluator != null){
			evaluator.init();

			try {
				List<Descriptor> descriptors = evaluator.getDescriptors();

				return (String)evaluator.evaluateAndFormat(mapValues(descriptors, parameters), null);
			} finally {
				evaluator.destroy();
			}
		} else

		{
			throw new DSpaceException("Model \'" + modelId + "\' is not evaluateable");
		}
	}

	private DSpaceException formatException(Exception e){
		log(e.getMessage(), e);

		if(e instanceof DSpaceException){
			return (DSpaceException)e;
		}

		return new DSpaceException(e.getMessage());
	}

	private PredictionColumn loadPredictionColumn(Prediction prediction) throws IOException {
		PredictionColumn column = new PredictionColumn();
		column.setId(prediction.getId());
		column.setName(prediction.getName());

		Map<String, Object> values = loadValues(prediction);
		column.setValues(values);
		column.setLength(parseLength(values));

		return column;
	}

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

	private DescriptorColumn loadDescriptorColumn(Descriptor descriptor, Collection<String> keys) throws IOException {
		DescriptorColumn column = new DescriptorColumn();
		column.setId(descriptor.getId());
		column.setName(descriptor.getName());

		Map<String, Object> values = loadValues(descriptor, keys);
		column.setValues(values);
		column.setLength(parseLength(values));
		column.setFormat(parseFormat(values));

		column.setCalculable(descriptor.hasCargo(BODOCargo.class));

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

	private IAtomContainer parseMolecule(String string) throws Exception {

		if(string.startsWith("InChI=")){
			return parseInChIMolecule(string);
		}

		return parseSmilesMolecule(string);
	}

	private IAtomContainer parseInChIMolecule(String string) throws CDKException, IOException {
		InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();

		InChIToStructure converter = factory.getInChIToStructure(string, DefaultChemObjectBuilder.getInstance());

		INCHI_RET status = converter.getReturnStatus();
		switch(status){
			case OKAY:
				break;
			default:
				throw new IOException();
		}

		IAtomContainer atomContainer = converter.getAtomContainer();
		if(!ConnectivityChecker.isConnected(atomContainer)){
			throw new IOException();
		}

		return new Molecule(atomContainer);
	}

	private IAtomContainer parseSmilesMolecule(String string) throws InvalidSmilesException {
		SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		return parser.parseSmiles(string);
	}

	private String calculateCdkDescriptor(IMolecularDescriptor descriptor, IAtomContainer molecule){
		DescriptorValue value = descriptor.calculate(molecule);

		IDescriptorResult result = value.getValue();

		if((result instanceof BooleanResult) || (result instanceof DoubleResult) || (result instanceof IntegerResult)){
			return result.toString();
		}

		throw new IllegalArgumentException(result.toString());
	}

	private <V> Map<Descriptor, V> mapValues(List<Descriptor> descriptors, Map<String, V> parameters){
		Map<Descriptor, V> values = new LinkedHashMap<Descriptor, V>();

		for(Descriptor descriptor : descriptors){
			values.put(descriptor, parameters.get(descriptor.getId()));
		}

		return values;
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
}