/*
 *  Copyright (c) 2014 University of Tartu
 */
package org.dspace.content;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.dmg.pmml.ClusteringModel;
import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.KohonenMap;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.RegressionModel;
import org.dmg.pmml.RegressionNormalizationMethodType;
import org.dmg.pmml.Segment;
import org.dmg.pmml.Segmentation;
import org.dmg.pmml.TreeModel;
import org.qsardb.cargo.map.FrequencyMap;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.cargo.pmml.PMMLCargo;
import org.qsardb.cargo.rds.RDSCargo;
import org.qsardb.cargo.rds.RDSObject;
import org.qsardb.evaluation.Evaluator;
import org.qsardb.evaluation.PMMLEvaluator;
import org.qsardb.model.Model;
import org.qsardb.model.Property;
import org.qsardb.model.Qdb;

public class QdbModelUtil {

	private static final Logger logger = Logger.getLogger(QdbModelUtil.class);

	private static final Map<String,String> modelTypeMap =
		ImmutableMap.<String,String>builder()
		.put("RegressionModel",           "Regression model")
		.put("GeneralRegressionModel",    "Regression model")
		.put("SupportVectorMachineModel", "Support vector machine")
		.put("NaiveBayesModel",           "Naive Bayes")
		.put("TreeModel",                 "Decision tree")
		.put("NeuralNetwork",             "Neural network")
		.put("NearestNeighborModel",      "k-Nearest neighbors")
		.put("AssociationModel",          "Association rules")
		.put("Scorecard",                 "Scorecard")
		.put("RuleSetModel",              "Ruleset")
		.build();

	public static Evaluator getEvaluator(Model model) throws Exception {
		Qdb qdb = model.getQdb();
		if (model.hasCargo(PMMLCargo.class)) {
			PMMLCargo pmmlCargo = model.getCargo(PMMLCargo.class);
			return new PMMLEvaluator(qdb, pmmlCargo.loadPmml());
		} else if (model.hasCargo(RDSCargo.class)) {
			RDSCargo rdsCargo = model.getCargo(RDSCargo.class);
			return new QdbRDSEvaluator(qdb, rdsCargo.loadRdsObject());
		}
		throw new IllegalArgumentException();
	}

	public static boolean isRegression(Model qdbModel) {
		String type = QdbModelUtil.detectType(qdbModel);
		if (type.toLowerCase().contains("classification")) {
			return false;
		} else
			if (type.toLowerCase().contains("regression")) {
			return true;
		}

		try {
			Property property = qdbModel.getProperty();
			Map<String, Double> map = property.getCargo(ValuesCargo.class).loadDoubleMap();
			HashSet<Double> uniq = new HashSet<>(map.values());
			return uniq.size() > 5;
		} catch (NumberFormatException ex) {
			return false;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String detectType(Model qdbModel) {
		if (qdbModel.hasCargo(PMMLCargo.class)){
			PMMLCargo pmmlCargo = qdbModel.getCargo(PMMLCargo.class);
			try {
				PMML pmml = pmmlCargo.loadPmml();

				String modelType = "";
				Map<FieldName, DataField> dataDictionary = Maps.newLinkedHashMap();
				for (DataField df: pmml.getDataDictionary().getDataFields()) {
					dataDictionary.put(df.getName(), df);

					String id = df.getName().getValue().replace("properties/", "");
					if (qdbModel.getProperty().getId().equals(id)){
						modelType = (df.getOptype() == OpType.CONTINUOUS) ? " (regression)" : " (classification)";
					}
				}

				org.dmg.pmml.Model m = pmml.getModels().get(0);
				return pmmlModelAsString(m, dataDictionary) + modelType;
			} catch (Exception ignored) {
				logger.warn("Unable to recognize PMML model type", ignored);
				return "";
			}
		}

		if (qdbModel.hasCargo(RDSCargo.class)) {
			RDSCargo rdsCargo = qdbModel.getCargo(RDSCargo.class);
			return rdsModelAsString(qdbModel.getQdb(), rdsCargo);
		}
		
		return "";
	}

	private static String pmmlModelAsString(org.dmg.pmml.Model pmmlModel, Map<FieldName, DataField> dataDictionary) {
		if (pmmlModel instanceof MiningModel) {
			MiningModel miningModel = (MiningModel) pmmlModel;
			Segmentation segmentation = miningModel.getSegmentation();
			if (segmentation == null) {
				return "";
			}

			FrequencyMap<Class> freq = new FrequencyMap<Class>();
			for (Segment segment: segmentation.getSegments()) {
				freq.add(segment.getModel().getClass());
			}
			
			Set<Class> classes = freq.getKeys();
			if (freq.getKeys().size() == 1) {
				if (classes.contains(TreeModel.class) && freq.getCountSum() > 10) {
					return "Random forest";
				}

				for (Segment segment: segmentation.getSegments()) {
					return pmmlModelAsString(segment.getModel(), dataDictionary) + " ensemble";
				}
			}
			
			return "Ensemble model";
		}
		
		if (pmmlModel instanceof ClusteringModel) {
			ClusteringModel clusteringModel = (ClusteringModel) pmmlModel;
			
			DataField havePredictedField = null;
			for (MiningField mf: clusteringModel.getMiningSchema().getMiningFields()) {
				if (mf.getUsageType().equals(FieldUsageType.PREDICTED)) {
					havePredictedField = dataDictionary.get(mf.getName());
					break;
				}
			}
			
			KohonenMap kohonen = clusteringModel.getClusters().get(0).getKohonenMap();
			if (havePredictedField != null && kohonen != null) {
				return "Counter-propagation neural network";
			}
			
			return "Clustering model";
		}

		if (pmmlModel instanceof RegressionModel) {
			RegressionModel model = (RegressionModel) pmmlModel;
			if (model.getNormalizationMethod() == RegressionNormalizationMethodType.LOGIT) {
				return "Logistic regression";
			}
		}
		
		String name = pmmlModel.getClass().getSimpleName();
		return modelTypeMap.get(name) != null ? modelTypeMap.get(name) : name;
	}

	private static String rdsModelAsString(Qdb qdb, RDSCargo rdsCargo) {
		try {
			RDSObject rdsObject = rdsCargo.loadRdsObject();
			QdbRDSEvaluator eval = new QdbRDSEvaluator(qdb, rdsObject);
			eval.init();

			try {
				String summary = eval.getSummary();
				return summary != null ? summary : "";
			} finally {
				eval.destroy();
			}
		} catch (Exception ignored) {
			logger.warn("Unable to recognize RDS model type", ignored);
			return "";
		}
	}
}
