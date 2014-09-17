package org.dspace.qsardb.client.gwt;

import java.util.*;

import org.dspace.qsardb.rpc.gwt.*;

import com.google.gwt.event.shared.*;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

public class SeriesMenuBar extends MenuBar {

	public SeriesMenuBar(List<PredictionColumn> predictions){
		super(true);

		addItemSection(PredictionColumn.filter(predictions, PredictionColumn.Type.TRAINING));
		addItemSection(PredictionColumn.filter(predictions, PredictionColumn.Type.VALIDATION));
		addItemSection(PredictionColumn.filter(predictions, PredictionColumn.Type.TESTING));

		Set<String> keys = new HashSet<String>();

		List<MenuItem> items = getItems();
		for(MenuItem item : items){

			if(item instanceof SeriesMenuItem){
				SeriesMenuItem seriesItem = (SeriesMenuItem)item;

				Map<String, ?> values = (seriesItem.getPrediction()).getValues();

				if(Collections.disjoint(keys, values.keySet())){
					seriesItem.toggle();
				}

				keys.addAll(values.keySet());
			}
		}

		fireSelectionChanged();
	}

	private void addItemSection(List<PredictionColumn> predictions){

		if(predictions.isEmpty()){
			return;
		} // End if

		List<MenuItem> items = getItems();
		if(items.size() > 0){
			addSeparator();
		}

		for(PredictionColumn prediction : predictions){
			SeriesMenuItem seriesItem = new SeriesMenuItem(prediction);

			addItem(seriesItem);
		}
	}

	public SeriesDisplayEvent createSeriesDisplayEvent(){
		return new SeriesDisplayEvent(getSelectedValue());
	}

	public HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler){
		return addHandler(handler, SeriesDisplayEvent.TYPE);
	}

	private void fireSelectionChanged(){
		fireEvent(createSeriesDisplayEvent());
	}

	public Map<PredictionColumn, Boolean> getSelectedValue(){
		Map<PredictionColumn, Boolean> result = new LinkedHashMap<PredictionColumn, Boolean>();

		List<MenuItem> items = getItems();
		for(MenuItem item : items){

			if(item instanceof SeriesMenuItem){
				SeriesMenuItem seriesItem = (SeriesMenuItem)item;

				result.put(seriesItem.getPrediction(), Boolean.valueOf(seriesItem.isSelected()));
			}
		}

		return result;
	}

	static
	private SafeHtml formatName(SeriesMenuItem item){
		return formatName(item.getPrediction(), item.isSelected());
	}

	static
	private SafeHtml formatName(PredictionColumn prediction, boolean selected){
		SafeHtmlBuilder builder = new SafeHtmlBuilder();

		String check = "<input type=\"checkbox\" " + (selected ? "checked>" : ">");
		builder.append(SafeHtmlUtils.fromSafeConstant(check));
		builder.append(SafeHtmlUtils.fromString(prediction.getName()));
		builder.append(SafeHtmlUtils.fromSafeConstant("</input>"));

		return builder.toSafeHtml();
	}

	private class SeriesMenuItem extends MenuItem implements Command {

		private PredictionColumn prediction = null;

		private boolean selected = false;


		private SeriesMenuItem(PredictionColumn prediction){
			super(formatName(prediction, false));

			setCommand(this);

			setPrediction(prediction);
		}

		@Override
		public void execute(){
			toggle();

			fireSelectionChanged();
		}

		public void toggle(){
			setSelected(!isSelected());

			setHTML(formatName(this));
		}

		public PredictionColumn getPrediction(){
			return this.prediction;
		}

		private void setPrediction(PredictionColumn prediction){
			this.prediction = prediction;
		}

		public boolean isSelected(){
			return this.selected;
		}

		private void setSelected(boolean selected){
			this.selected = selected;
		}
	}
}