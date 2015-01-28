package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.io.*;
import java.math.*;
import java.util.*;

import org.qsardb.cargo.bibtex.*;
import org.qsardb.model.*;
import org.qsardb.model.Container;

import org.jbibtex.*;

import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.content.*;
import org.dspace.content.QdbUtil;
import org.dspace.content.citation.ACSReferenceStyle;
import org.dspace.content.citation.ReferenceFormatter;
import org.dspace.core.*;
import org.dspace.qsardb.service.StatisticsUtil;
import org.dspace.qsardb.service.StatisticsUtil.StringValues;
import org.dspace.qsardb.service.StatisticsUtil.Values;

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

		if (properties.size() == 0) {
			division.addPara("This archive contains no properties");
		} else {
			for(Property property : properties){
				generatePropertyDivision(viewer, item, qdb, property, division);
			}
		}

	}

	static
	private void generatePropertyDivision(ItemViewer viewer, org.dspace.content.Item item, Qdb qdb, Property property, Division division) throws IOException, WingException {
		ModelRegistry models = qdb.getModelRegistry();
		PredictionRegistry predictions = qdb.getPredictionRegistry();

		Division propertyDivision = division.addDivision("property-" + property.getId(), "secondary");
		Head propertyHead = propertyDivision.setHead();
		propertyHead.addContent(T_property_head.parameterize(property.getId(), property.getName()));
		addDescriptionInfo(property, propertyHead);

		Values<?> propertyValues = StatisticsUtil.loadValues(property);
		boolean isClassification = propertyValues instanceof StringValues;

		Map<String, BibTeXEntry> bibliography = new LinkedHashMap<String, BibTeXEntry>();

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
			String target = viewer.getContextPath() + "/compounds/" + item.getHandle() + "?property=" + property.getId();
			valuesPara.addXref(target, T_property_values.parameterize(propertyValues.size()));
		}

		if(propertyModels.size() > 0 && propertyPredictions.size() > 0){
			Division propertyModelsDivision = propertyDivision.addDivision("property-models-summary-" + property.getId(), "secondary");
			propertyModelsDivision.setHead(T_property_models_summary.parameterize(propertyModels.size(), propertyPredictions.size())); // XXX

			for(Model propertyModel : propertyModels){
				Division modelDivision = propertyModelsDivision.addDivision("model-" + propertyModel.getId(), "secondary");
				Head modelHead = modelDivision.setHead();
				modelHead.addContent(T_model_summary.parameterize(propertyModel.getId(), propertyModel.getName()));
				addDescriptionInfo(propertyModel, modelHead);

				Para summaryPara = modelDivision.addPara("model-summary", "side-left");
				summaryPara.addContent(loadSummary(propertyModel));

				Para applicationPara = modelDivision.addPara("model-application", "side-right");
				applicationPara.addContent("Open in:");
				applicationPara.addXref(viewer.getContextPath() + "/explorer/" + item.getHandle() + "?model=" + propertyModel.getId(), "QDB Explorer", "application-link");
				applicationPara.addXref(viewer.getContextPath() + "/predictor/" + item.getHandle() + "?model=" + propertyModel.getId(), "QDB Predictor", "application-link");

				java.util.Collection<Prediction> modelPredictions = predictions.getByModel(propertyModel);


				int columns = isClassification ? 4 : 5;
				Table modelTable = modelDivision.addTable("model-summary-" + propertyModel.getId(), modelPredictions.size(), columns);

				if(true){
					Row headerRow = modelTable.addRow(Row.ROLE_HEADER);

					Cell nameCell = headerRow.addCell(null, Cell.ROLE_HEADER, null);
					nameCell.addContent("Name");

					Cell typeCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
					typeCell.addContent("Type");

					Cell sizeCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
					sizeCell.addContent("n");

					if (isClassification) {
						Cell accCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
						accCell.addContent("Accuracy");
					} else {
						Cell rsqCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
						rsqCell.addHtmlContent("<p>R<sup>2</sup></p>");

						Cell stdevCell = headerRow.addCell(null, Cell.ROLE_HEADER, "short");
						stdevCell.addHtmlContent("<p>&#x3c3;</p>");
					}
				}

				Values<?> trainingValues = null;

				for(Prediction modelPrediction : modelPredictions){
					Values<?> predictionValues = StatisticsUtil.loadValues(modelPrediction);

					if((modelPrediction.getType()).equals(Prediction.Type.TRAINING)){
						trainingValues = predictionValues;
					}

					Row predictionRow = modelTable.addRow(Row.ROLE_DATA);

					Cell nameCell = predictionRow.addCell();
					nameCell.addContent(modelPrediction.getName());
					addDescriptionInfo(modelPrediction, nameCell);
					predictionRow.addCellContent(formatPredictionType(modelPrediction.getType(), trainingValues, predictionValues));
					predictionRow.addCellContent(String.valueOf(predictionValues.size()));
					if (isClassification) {
						predictionRow.addCellContent(formatStats(predictionValues.accuracy(propertyValues)));
					} else {
						predictionRow.addCellContent(formatStats(predictionValues.rsq(propertyValues)));
						predictionRow.addCellContent(formatStats(predictionValues.stdev(propertyValues)));
					}
				}
			}
		} // End if

		if(bibliography.size() > 0){
			List bibliographyList = propertyDivision.addList("property-bibliography-" + property.getId(), null);
			bibliographyList.setHead(T_property_bibliography);

			ArrayList<String> keys = new ArrayList<String>(bibliography.keySet());
			Collections.sort(keys);

			ReferenceFormatter formatter = new ReferenceFormatter(new ACSReferenceStyle());

			for(String key : keys){
				BibTeXEntry entry = bibliography.get(key);

				Item referencePara = bibliographyList.addItem();
				referencePara.addHtmlContent(formatter.format(entry, true));
			}
		}
	}

	private static void addDescriptionInfo(Container container, TextContainer target) throws WingException {
		String description = container.getDescription();
		if (description != null && !description.trim().isEmpty()) {
			target.addInfo("Description", description);
		}
	}

	static
	private Map<String, BibTeXEntry> loadBibliography(Container<?, ?> container){

		if(container.hasCargo(BibTeXCargo.class)){
			BibTeXCargo bibtexCargo = container.getCargo(BibTeXCargo.class);

			try {
				BibTeXDatabase database = bibtexCargo.loadBibTeX();
				Map<String, BibTeXEntry> m = new LinkedHashMap<String, BibTeXEntry>();

				for (BibTeXEntry e: database.getEntries().values()) {
					m.put(createSortableEntryKey(e), e);
				}
				return m;
			} catch(Exception e){
				// Ignored
			}
		}

		return Collections.<String, BibTeXEntry>emptyMap();
	}

	private static String createSortableEntryKey (BibTeXEntry e) {
		String year = getKey(e, BibTeXEntry.KEY_YEAR, "0000");
		String author = getKey(e, BibTeXEntry.KEY_AUTHOR, "unknown").split(",")[0];
		String pages = getKey(e, BibTeXEntry.KEY_PAGES, "");
		String doi = getKey(e, BibTeXEntry.KEY_DOI, "");

		return year + author + pages + doi;
	}

	private static String getKey(BibTeXEntry entry, Key key, String missing) {
		org.jbibtex.Value value = entry.getField(key);
		return value != null ? value.toUserString() : missing;
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

	private static String formatStats(BigDecimal val) {
		return val != null ? String.valueOf(val) : "N/A";
	}

	static
	private String loadSummary(Model model){
		String type = QdbModelUtil.detectType(model);
		return type.isEmpty() ? "(Unknown model type)" : type;
	}

	private static final Message T_property_head = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.head_property");

	private static final Message T_property_summary = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.property_summary");

	private static final Message T_property_values = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.property_values");

	private static final Message T_property_models_summary = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.property_models_summary");

	private static final Message T_model_summary = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.model_summary");

	private static final Message T_property_bibliography = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.property_bibliography");
}
