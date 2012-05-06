package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.io.*;
import java.math.*;
import java.util.*;

import org.qsardb.cargo.bibtex.*;
import org.qsardb.cargo.map.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;
import org.qsardb.model.Container;

import org.jbibtex.*;
import org.jbibtex.citation.*;

import org.apache.commons.math.stat.descriptive.*;
import org.apache.commons.math.stat.regression.*;

import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.content.*;
import org.dspace.content.QdbUtil;
import org.dspace.core.*;

class ItemContentPanel {

	private ItemContentPanel(){
	}

	static
	public void generate(final ItemViewer viewer, final org.dspace.content.Item item, final Division division) throws IOException, WingException {
		Context context = viewer.getContext();

		QdbCallable<Object> callable = new QdbCallable<Object>(){

			@Override
			public Object call(Qdb qdb) throws IOException, WingException {
				generate(viewer, item, qdb, division);

				return null;
			}
		};

		try {
			QdbUtil.invokeInternal(context, item, callable);
		} catch(IOException ioe){
			throw ioe;
		} catch(WingException we){
			throw we;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	static
	public void generate(ItemViewer viewer, org.dspace.content.Item item, Qdb qdb, Division division) throws IOException, WingException {
		PropertyRegistry properties = qdb.getPropertyRegistry();

		for(Property property : properties){
			generatePropertyDivision(viewer, item, qdb, property, division);
		}
	}

	static
	private void generatePropertyDivision(ItemViewer viewer, org.dspace.content.Item item, Qdb qdb, Property property, Division division) throws IOException, WingException {
		ModelRegistry models = qdb.getModelRegistry();
		PredictionRegistry predictions = qdb.getPredictionRegistry();

		Division propertyDivision = division.addDivision("property-" + property.getId(), "secondary");
		propertyDivision.setHead(T_property_head.parameterize(property.getId(), property.getName()));

		Values<?> propertyValues = loadValues(property);

		Map<Key, BibTeXEntry> bibliography = new LinkedHashMap<Key, BibTeXEntry>();

		bibliography.putAll(loadBibliography(property));

		java.util.Collection<Model> propertyModels = models.getByProperty(property);
		java.util.Collection<Prediction> propertyPredictions = new LinkedHashSet<Prediction>();

		for(Model propertyModel : propertyModels){
			bibliography.putAll(loadBibliography(propertyModel));

			java.util.Collection<Prediction> modelPredictions = predictions.getByModel(propertyModel);

			propertyPredictions.addAll(modelPredictions);
		}

		for(Prediction propertyPrediction : propertyPredictions){
			bibliography.putAll(loadBibliography(propertyPrediction));
		}

		if(true){
			Division summaryDivision = propertyDivision.addDivision("property-summary-" + property.getId(), "secondary");
			summaryDivision.setHead(T_property_summary);

			Para valuesPara = summaryDivision.addPara("property-values", null);
			valuesPara.addContent(T_property_values.parameterize(propertyValues.size()));
		} // End if

		if(propertyModels.size() > 0 && propertyPredictions.size() > 0){
			Division propertyModelsDivision = propertyDivision.addDivision("property-models-summary-" + property.getId(), "secondary");
			propertyModelsDivision.setHead(T_property_models_summary.parameterize(propertyModels.size(), propertyPredictions.size())); // XXX

			for(Model propertyModel : propertyModels){
				Division modelDivision = propertyModelsDivision.addDivision("model-" + propertyModel.getId(), "secondary");
				modelDivision.setHead(T_model_summary.parameterize(propertyModel.getId(), propertyModel.getName()));

				Para summaryPara = modelDivision.addPara("model-summary", "side-left");
				summaryPara.addContent(loadSummary(propertyModel));

				Para applicationPara = modelDivision.addPara("model-application", "side-right");
				applicationPara.addContent("Open in:");
				applicationPara.addXref(viewer.getContextPath() + "/explorer/" + item.getHandle() + "?model=" + propertyModel.getId(), "QDB Explorer", "application-link");
				applicationPara.addXref(viewer.getContextPath() + "/predictor/" + item.getHandle() + "?model=" + propertyModel.getId(), "QDB Predictor", "application-link");

				java.util.Collection<Prediction> modelPredictions = predictions.getByModel(propertyModel);

				Table modelTable = modelDivision.addTable("model-summary-" + propertyModel.getId(), modelPredictions.size(), 5);

				if(true){
					Row headerRow = modelTable.addRow("header");

					Cell nameCell = headerRow.addCell(null, Cell.ROLE_HEADER, null);
					nameCell.addContent("Name");

					Cell typeCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
					typeCell.addContent("Type");

					Cell sizeCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
					sizeCell.addContent("n");

					Cell rsqCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
					rsqCell.addHtmlContent("<p>R<sup>2</sup></p>");

					Cell stdevCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
					stdevCell.addHtmlContent("<p>&#x3c3;</p>");
				}

				Values<?> trainingValues = null;

				for(Prediction modelPrediction : modelPredictions){
					Values<?> predictionValues = loadValues(modelPrediction);

					if((modelPrediction.getType()).equals(Prediction.Type.TRAINING)){
						trainingValues = predictionValues;
					}

					Row predictionRow = modelTable.addRow("data");

					predictionRow.addCellContent(modelPrediction.getName());
					predictionRow.addCellContent(formatPredictionType(modelPrediction.getType(), trainingValues, predictionValues));
					predictionRow.addCellContent(String.valueOf(predictionValues.size()));
					predictionRow.addCellContent(String.valueOf(predictionValues.rsq(propertyValues)));
					predictionRow.addCellContent(String.valueOf(predictionValues.stdev(propertyValues)));
				}
			}
		} // End if

		if(bibliography.size() > 0){
			List bibliographyList = propertyDivision.addList("property-bibliography-" + property.getId(), null);
			bibliographyList.setHead(T_property_bibliography);

			java.util.List<Key> keys = new ArrayList<Key>(bibliography.keySet());

			Comparator<Key> comparator = new Comparator<Key>(){

				@Override
				public int compare(Key left, Key right){
					return ((left.getValue()).toLowerCase()).compareTo((right.getValue()).toLowerCase());
				}
			};
			Collections.sort(keys, comparator);

			ReferenceFormatter formatter = new ReferenceFormatter(new ACSReferenceStyle());

			for(Key key : keys){
				BibTeXEntry entry = bibliography.get(key);

				Item referencePara = bibliographyList.addItem("property-reference-" + key.getValue(), null);
				referencePara.addHtmlContent(formatter.format(entry, true));
			}
		}
	}

	static
	private Values<?> loadValues(Parameter<?, ?> parameter) throws IOException {

		if(parameter.hasCargo(ValuesCargo.class)){
			ValuesCargo valuesCargo = parameter.getCargo(ValuesCargo.class);

			try {
				FlexBigDecimalFormat valueFormat = new FlexBigDecimalFormat();

				Map<String, BigDecimal> values = valuesCargo.loadMap(valueFormat);

				if(valueFormat.isValid()){
					return new BigDecimalValues(values);
				}
			} catch(Exception e){
				// Ignored
			}

			return new StringValues(valuesCargo.loadStringMap());
		}

		return new StringValues(Collections.<String, String>emptyMap());
	}

	static
	private Map<Key, BibTeXEntry> loadBibliography(Container<?, ?> container) throws IOException {

		if(container.hasCargo(BibTeXCargo.class)){
			BibTeXCargo bibtexCargo = container.getCargo(BibTeXCargo.class);

			try {
				BibTeXDatabase database = bibtexCargo.loadBibTeX();

				return database.getEntries();
			} catch(Exception e){
				// Ignored
			}
		}

		return Collections.<Key, BibTeXEntry>emptyMap();
	}

	static
	private String formatPredictionType(Prediction.Type type, Values<?> trainingValues, Values<?> values){

		switch(type){
			case TRAINING:
				return "training";
			case VALIDATION:
				if(trainingValues != null){
					Set<String> trainingKeys = trainingValues.keySet();
					Set<String> keys = values.keySet();

					if((trainingKeys).containsAll(keys)){
						return "internal validation";
					} else

					if(Collections.disjoint(trainingKeys, keys)){
						return "external validation";
					}
				}
				return "validation";
			case TESTING:
				return "testing";
		}

		return null;
	}

	static
	private String loadSummary(Model model){

		try {
			Evaluator evaluator = getEvaluator(model);

			if(evaluator != null){
				evaluator.init();

				try {
					String summary = evaluator.getSummary();
					if(summary != null){
						return summary;
					}
				} finally {
					evaluator.destroy();
				}
			}
		} catch(Exception e){
			// Ignored
		}

		return "Unknown";
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
	private class Values<X> {

		private Map<String, X> values = null;


		private Values(Map<String, X> values){
			this.values = values;
		}

		public int size(){
			return this.values.size();
		}

		public BigDecimal rsq(Values<?> values){
			return null;
		}

		public BigDecimal stdev(Values<?> values){
			return null;
		}

		public X get(String key){
			return this.values.get(key);
		}

		public Set<String> keySet(){
			return this.values.keySet();
		}

		public java.util.Collection<X> values(){
			return this.values.values();
		}
	}

	static
	private class StringValues extends Values<String> {

		public StringValues(Map<String, String> values){
			super(values);
		}
	}

	static
	private class BigDecimalValues extends Values<BigDecimal> {

		public BigDecimalValues(Map<String, BigDecimal> values){
			super(values);
		}

		@Override
		public BigDecimal rsq(Values<?> values){

			if(values instanceof BigDecimalValues){
				BigDecimalValues that = (BigDecimalValues)values;

				SimpleRegression regression = new SimpleRegression();

				Set<String> keys = new LinkedHashSet<String>(this.keySet());
				keys.retainAll(that.keySet());

				for(String key : keys){
					BigDecimal thisValue = this.get(key);
					BigDecimal thatValue = that.get(key);

					if(thisValue == null || thatValue == null){
						continue;
					}

					regression.addData(thisValue.doubleValue(), thatValue.doubleValue());
				}

				BigDecimal result = new BigDecimal(regression.getRSquare());
				result = result.setScale(3, RoundingMode.HALF_UP);

				return result;
			}

			return super.rsq(values);
		}

		@Override
		public BigDecimal stdev(Values<?> values){

			if(values instanceof BigDecimalValues){
				BigDecimalValues that = (BigDecimalValues)values;

				DescriptiveStatistics statistic = new DescriptiveStatistics();

				Set<String> keys = new LinkedHashSet<String>(this.keySet());
				keys.retainAll(that.keySet());

				for(String key : keys){
					BigDecimal thisValue = this.get(key);
					BigDecimal thatValue = that.get(key);

					if(thisValue == null || thatValue == null){
						continue;
					}

					statistic.addValue((thisValue).subtract(thatValue).doubleValue());
				}

				BigDecimal result = new BigDecimal(statistic.getStandardDeviation());
				result = result.setScale(3, RoundingMode.HALF_UP);

				return result;
			}

			return super.stdev(values);
		}
	}

	static
	private class FlexBigDecimalFormat extends BigDecimalFormat {

		private boolean valid = false;


		@Override
		public BigDecimal parseString(String string){

			if(isText(string)){
				return null;
			}

			BigDecimal result = super.parseString(string);

			this.valid |= true;

			return result;
		}

		private boolean isValid(){
			return this.valid;
		}

		static
		private boolean isText(String string){

			for(int i = 0; string != null && i < string.length(); i++){
				char c = string.charAt(i);

				if(!Character.isLetter(c)){
					return false;
				}
			}

			return true;
		}
	}

	private static final Message T_property_head = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.head_property");

	private static final Message T_property_summary = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.property_summary");

	private static final Message T_property_values = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.property_values");

	private static final Message T_property_models_summary = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.property_models_summary");

	private static final Message T_model_summary = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.model_summary");

	private static final Message T_property_bibliography = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.property_bibliography");
}