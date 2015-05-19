package org.dspace.qsardb.service;

import java.util.*;

import org.qsardb.cargo.bodo.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

import net.sf.blueobelisk.*;
import net.sf.jniinchi.*;

import org.dspace.content.QdbUtil;
import org.openscience.cdk.*;
import org.openscience.cdk.exception.*;
import org.openscience.cdk.inchi.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.qsar.*;
import org.openscience.cdk.smiles.*;

public class PredictorUtil {

	private PredictorUtil(){
	}

	static
	synchronized
	public Map<String, String> calculateDescriptors(Model model, String structure) throws Exception {
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
	public String evaluate(Model model, Map<String, String> parameters, String structure) throws Exception {
		IAtomContainer molecule = prepareMolecule(structure);

		Evaluator evaluator = prepareEvaluator(model);

		evaluator.init();

		try {
			parameters.putAll(calculateDescriptors(evaluator, molecule));

			return evaluate(evaluator, parameters);
		} finally {
			evaluator.destroy();
		}
	}

	static
	synchronized
	public String evaluate(Model model, Map<String, String> parameters) throws Exception {
		Evaluator evaluator = prepareEvaluator(model);

		evaluator.init();

		try {
			return evaluate(evaluator, parameters);
		} finally {
			evaluator.destroy();
		}
	}

	static
	private Map<String, String> calculateDescriptors(Evaluator evaluator, IAtomContainer molecule) throws Exception {
		Map<String, String> result = new LinkedHashMap<String, String>();

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

			result.put(descriptor.getId(), String.valueOf(value));
		}

		return result;
	}

	static
	private String evaluate(Evaluator evaluator, Map<String, String> parameters) throws Exception {
		List<Descriptor> descriptors = evaluator.getDescriptors();

		return (String)evaluator.evaluateAndFormat(mapValues(descriptors, parameters), null);
	}

	static
	public IAtomContainer prepareMolecule(String string) throws CDKException {
		IAtomContainer molecule = parseMolecule(string);

		molecule = DescriptorUtil.prepareMolecule(molecule);

		return molecule;
	}

	static
	private Evaluator prepareEvaluator(Model model) throws Exception {
		Evaluator evaluator = QdbUtil.getEvaluator(model);

		if(evaluator == null){
			throw new IllegalArgumentException("Model \'" + model.getId() + "\' is not evaluateable");
		}

		return evaluator;
	}

	static
	private IAtomContainer parseMolecule(String string) throws CDKException {

		if(string.startsWith("InChI=")){
			return parseInChIMolecule(string);
		}

		return parseSmilesMolecule(string);
	}

	static
	private IAtomContainer parseInChIMolecule(String string) throws CDKException {
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

	static
	private <V> Map<Descriptor, V> mapValues(List<Descriptor> descriptors, Map<String, V> parameters){
		Map<Descriptor, V> values = new LinkedHashMap<Descriptor, V>();

		for(Descriptor descriptor : descriptors){
			values.put(descriptor, parameters.get(descriptor.getId()));
		}

		return values;
	}
}
