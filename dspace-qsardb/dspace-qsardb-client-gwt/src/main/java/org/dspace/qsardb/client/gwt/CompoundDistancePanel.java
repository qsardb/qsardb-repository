package org.dspace.qsardb.client.gwt;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.reveregroup.gwt.imagepreloader.ImageLoadEvent;
import com.reveregroup.gwt.imagepreloader.ImageLoadHandler;
import com.reveregroup.gwt.imagepreloader.ImagePreloader;
import java.util.List;
import org.dspace.qsardb.rpc.gwt.Analogue;
import org.dspace.qsardb.rpc.gwt.AttributeColumn;
import org.dspace.qsardb.rpc.gwt.DescriptionColumn;
import org.dspace.qsardb.rpc.gwt.LabelsColumn;
import org.dspace.qsardb.rpc.gwt.ModelTable;
import org.dspace.qsardb.rpc.gwt.ParameterColumn;
import org.dspace.qsardb.rpc.gwt.PredictionColumn;
import org.dspace.qsardb.rpc.gwt.PropertyColumn;

public class CompoundDistancePanel extends Composite implements EvaluationEventHandler {
	private final ModelTable table;
	private final FlowPanel panel;

	CompoundDistancePanel(ModelTable table) {
		this.table = table;
		this.panel = new FlowPanel();
		panel.getElement().setId("compound-distance-panel");
		panel.add(new HTML("Loading ..."));
		initWidget(panel);
	}

	@Override
	public void onEvaluate(EvaluationEvent event) {
		panel.clear();

		List<Analogue> analogues = event.getResponse().getTrainingAnalogues();
		if (analogues.isEmpty()) {
			return;
		}

		Resolver resolver = new Resolver(table);

		DescriptionColumn description = table.getColumn(DescriptionColumn.class);
		LabelsColumn labels = table.getColumn(LabelsColumn.class);

		PropertyColumn property = table.getColumn(PropertyColumn.class);
		NumberFormat fmt = NumberFormat.getFormat("0.0000");
		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);

		for (int i=0; i<Math.min(5, analogues.size()); i++) {
			Analogue analogue = analogues.get(i);
			String compId = analogue.getCompoundId();
			double distance = analogue.getDistance();

			String setID = "N/A";
			String predictedValue = "N/A";
			for (PredictionColumn p: predictions) {
				if (p.getValues().containsKey(compId)) {
					setID = p.getId();
					predictedValue = fmtParameter(p,compId);
					break;
				}
			}

			if (i > 0) {
				panel.add(new HTML("<hr />"));
			}

			FlowPanel analogueDiv = new FlowPanel();

			final FlowPanel imgDiv = new FlowPanel();
			imgDiv.setStylePrimaryName("analogue-depict");
			analogueDiv.add(imgDiv);
			String depictionUrl = resolver.resolveURL(compId) + "&width=240&height=180";
			ImagePreloader.load(depictionUrl, new ImageLoadHandler() {
				@Override
				public void imageLoaded(ImageLoadEvent event) {
					if (!event.isLoadFailed()) {
						imgDiv.add(new Image(event.getImageUrl()));
					}
				}
			});

			FlexTable analogueAttributes = new FlexTable();
			analogueAttributes.setStylePrimaryName("analogue-attr");

			int row = -1;
			analogueAttributes.setText(++row, 0, "Id:");
			analogueAttributes.setText(row, 1, compId);

			analogueAttributes.setText(++row, 0, "Name:");
			analogueAttributes.setText(row, 1, resolver.getName(compId));

			analogueAttributes.setText(++row, 0, "CAS:");
			analogueAttributes.setText(row, 1, resolver.getCas(compId));

			analogueAttributes.setText(++row, 0, "Set:");
			analogueAttributes.setText(row, 1, setID);

			if (hasAttribute(description, compId)) {
				analogueAttributes.setText(++row, 0, "Description:");
				analogueAttributes.setText(row, 1, description.getValue(compId));
			}

			if (hasAttribute(labels, compId)) {
				analogueAttributes.setText(++row, 0, "Labels:");
				analogueAttributes.setText(row, 1, labels.getValue(compId));
			}

			analogueDiv.add(analogueAttributes);

			FlexTable analogueValues = new FlexTable();
			analogueValues.setStylePrimaryName("analogue-values");

			row=-1;
			analogueValues.setText(++row, 0, "Distance:");
			analogueValues.setText(row, 1, fmt.format(distance));

			analogueValues.setText(++row, 0, "Experimental:");
			analogueValues.setText(row, 1, fmtParameter(property, compId));

			analogueValues.setText(++row, 0, "Calculated:");
			analogueValues.setText(row, 1, predictedValue);

			analogueDiv.add(analogueValues);

			panel.add(analogueDiv);
		}
	}

	private String fmtParameter(ParameterColumn col, String key) {
		Object v = col.getValue(key);
		return v != null ? v.toString() : "N/A";
	}

	private boolean hasAttribute(AttributeColumn col, String key) {
		if (col != null) {
			String v = col.getValue(key);
			return v == null ? false : !v.isEmpty();
		}
		return false;
	}
}
