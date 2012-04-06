package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.io.*;
import java.math.*;
import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

import org.apache.commons.math.stat.descriptive.*;
import org.apache.commons.math.stat.regression.*;

import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.content.*;
import org.dspace.content.Item;
import org.dspace.core.*;

class ItemContentPanel {

	private ItemContentPanel(){
	}

	static
	public void generate(final ItemViewer viewer, final Item item, final Division division) throws IOException, WingException {
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
	public void generate(ItemViewer viewer, Item item, Qdb qdb, Division division) throws IOException, WingException {
		PropertyRegistry properties = qdb.getPropertyRegistry();
		ModelRegistry models = qdb.getModelRegistry();
		PredictionRegistry predictions = qdb.getPredictionRegistry();

		for(Property property : properties){
			Division propertyDivision = division.addDivision("property-" + property.getId(), "secondary");
			propertyDivision.setHead(T_property_head.parameterize(property.getId(), property.getName()));

			Para propertyValuesPara = propertyDivision.addPara("property-values", null);

			Values<?> propertyValues = loadValues(property);
			propertyValuesPara.addContent(T_property_values.parameterize(propertyValues.size()));

			java.util.Collection<Model> propertyModels = models.getByProperty(property);
			if(propertyModels.isEmpty()){
				continue;
			}

			int rows = 1;
			int columns = 4;

			for(Model propertyModel : propertyModels){
				rows += 1;

				java.util.Collection<Prediction> modelPredictions = predictions.getByModel(propertyModel);
				rows += modelPredictions.size();
			}

			Table modelsTable = division.addTable("property-models-" + property.getId(), rows, columns);
			modelsTable.setHead(T_property_models_head.parameterize(propertyModels.size()));

			Row headerRow = modelsTable.addRow("header");

			{
				Cell nameCell = headerRow.addCell(null, Cell.ROLE_HEADER, null);
				nameCell.addContent((String)null);

				Cell sizeCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
				sizeCell.addContent("n");

				Cell rsqCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
				rsqCell.addHtmlContent("<p>R<sup>2</sup></p>");

				Cell stdevCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
				stdevCell.addHtmlContent("<p>&#x3c3;</p>");
			}

			for(Model propertyModel : propertyModels){
				Row modelRow = modelsTable.addRow("model-" + propertyModel.getId(), "header", "subheader");

				Cell modelSummary = modelRow.addCell(null, null, 1, columns, null);
				modelSummary.addContent(T_model_summary.parameterize(propertyModel.getId(), propertyModel.getName()));
				modelSummary.addXref(viewer.getContextPath() + "/explorer/" + item.getHandle() + "?model=" + propertyModel.getId(), T_model_explorer, "application-link");
				modelSummary.addXref(viewer.getContextPath() + "/predictor/" + item.getHandle() + "?model=" + propertyModel.getId(), T_model_predictor, "application-link");

				java.util.Collection<Prediction> modelPredictions = predictions.getByModel(propertyModel);
				for(Prediction modelPrediction : modelPredictions){
					Row predictionRow = modelsTable.addRow("data");

					Values predictionValues = loadValues(modelPrediction);

					predictionRow.addCellContent(T_prediction_name.parameterize(modelPrediction.getName()));
					predictionRow.addCellContent(T_prediction_values.parameterize(predictionValues.size()));
					predictionRow.addCellContent(T_prediction_rsq.parameterize(predictionValues.rsq(propertyValues)));
					predictionRow.addCellContent(T_prediction_stdev.parameterize(predictionValues.stdev(propertyValues)));
				}
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

		throw new IOException();
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

	private static final Message T_property_head = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.head_property");

	private static final Message T_property_values = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.property_values");

	private static final Message T_property_models_head = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.head_property_models");

	private static final Message T_model_summary = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.model_summary");

	private static final Message T_model_explorer = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.model_explorer");

	private static final Message T_model_predictor = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.model_predictor");

	private static final Message T_prediction_name = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.prediction_name");

	private static final Message T_prediction_values = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.prediction_values");

	private static final Message T_prediction_rsq = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.prediction_rsq");

	private static final Message T_prediction_stdev = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.prediction_stdev");
}