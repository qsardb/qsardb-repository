package org.dspace.qsardb.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.sf.blueobelisk.BODODescriptor;
import net.sf.jniinchi.INCHI_RET;
import org.dspace.content.QdbModelUtil;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.BODOUtil;
import org.openscience.cdk.qsar.DescriptorUtil;
import org.openscience.cdk.qsar.DescriptorValueCache;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.smiles.SmilesParser;
import org.qsardb.cargo.bodo.BODOCargo;
import org.qsardb.evaluation.Evaluator;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Model;

public class PredictorUtil {

	public static class Result {
		private String value;
		private String equation;

		public String getValue() {
			return value;
		}

		public String getEquation() {
			return equation;
		}
	}

	private PredictorUtil() {
	}

	public static synchronized Map<Descriptor, String> calculateDescriptors(Model model, String structure) throws Exception {
		IAtomContainer molecule = prepareMolecule(structure);

		Evaluator evaluator = prepareEvaluator(model);
		evaluator.init();

		try {
			return calculateDescriptors(evaluator, molecule);
		} finally {
			evaluator.destroy();
		}
	}

	static
	synchronized
	public Result evaluate(Model model, Map<String, String> parameters) throws Exception {
		Evaluator evaluator = prepareEvaluator(model);

		evaluator.init();

		try {
			return evaluate(evaluator, parameters);
		} finally {
			evaluator.destroy();
		}
	}

	private static Map<Descriptor, String> calculateDescriptors(Evaluator evaluator, IAtomContainer molecule) throws Exception {
		Map<Descriptor, String> result = new LinkedHashMap<>();

		DescriptorValueCache cache = new DescriptorValueCache();

		List<Descriptor> descriptors = evaluator.getDescriptors();
		for(Descriptor descriptor : descriptors){

			if(!descriptor.hasCargo(BODOCargo.class)){
				continue;
			}

			BODOCargo bodoCargo = descriptor.getCargo(BODOCargo.class);

			BODODescriptor bodoDescriptor = bodoCargo.loadBodoDescriptor();

			IMolecularDescriptor cdkDescriptor = (IMolecularDescriptor)BODOUtil.parse(bodoDescriptor);

			Object value = cache.calculate(cdkDescriptor, molecule);

			result.put(descriptor, String.valueOf(value));

		}

		return result;
	}

	static
	private Result evaluate(Evaluator evaluator, Map<String, String> parameters) throws Exception {
		List<Descriptor> descriptors = evaluator.getDescriptors();
		Map<Descriptor, String> descriptorValues = mapValues(descriptors, parameters);

		Result r = new Result();
		r.equation = (String)evaluator.evaluateAndFormat(descriptorValues, null);

		Object result = evaluator.evaluate(descriptorValues).getValue();
		if (result instanceof Map) {
			Map map = (Map)result;
			if (map.size() == 1) {
				result = map.values().iterator().next();
			}
		}

		r.value = String.valueOf(result);
		return r;
	}

	public static IAtomContainer prepareMolecule(String string) throws CDKException {
		IAtomContainer molecule = parseMolecule(string);

		molecule = DescriptorUtil.prepareMolecule(molecule);

		return molecule;
	}

	private static Evaluator prepareEvaluator(Model model) throws Exception {
		Evaluator evaluator = QdbModelUtil.getEvaluator(model);

		if(evaluator == null){
			throw new IllegalArgumentException("Model \'" + model.getId() + "\' is not evaluateable");
		}

		return evaluator;
	}

	private static IAtomContainer parseMolecule(String string) throws CDKException {

		if(string.startsWith("InChI=")){
			return parseInChIMolecule(string);
		}

		return parseSmilesMolecule(string);
	}

	private static IAtomContainer parseInChIMolecule(String string) throws CDKException {
		InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();

		InChIToStructure converter = factory.getInChIToStructure(string, DefaultChemObjectBuilder.getInstance());

		INCHI_RET status = converter.getReturnStatus();
		switch(status){
			case OKAY:
				break;
			default:
				throw new CDKException("Invalid InChI");
		}

		return converter.getAtomContainer();
	}

	static
	private IAtomContainer parseSmilesMolecule(String string) throws InvalidSmilesException {
		SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		return parser.parseSmiles(string);
	}

	private static <V> Map<Descriptor, V> mapValues(List<Descriptor> descriptors, Map<String, V> parameters){
		Map<Descriptor, V> values = new LinkedHashMap<>();

		for(Descriptor descriptor : descriptors){
			values.put(descriptor, parameters.get(descriptor.getId()));
		}

		return values;
	}
}
