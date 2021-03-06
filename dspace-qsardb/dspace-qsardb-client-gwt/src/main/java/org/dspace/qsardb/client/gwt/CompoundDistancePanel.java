package org.dspace.qsardb.client.gwt;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.*;
import com.reveregroup.gwt.imagepreloader.*;
import java.util.*;
import org.dspace.qsardb.rpc.gwt.*;

public class CompoundDistancePanel extends Composite implements EvaluationEventHandler {
	private final ModelTable table;
	private final CompoundDistances distances;
	private final FlowPanel panel;

	CompoundDistancePanel(ModelTable table) {
		this.table = table;
		this.panel = new FlowPanel();
		PropertyColumn property = table.getColumn(PropertyColumn.class);
		Set<String> compIds = property.getValues().keySet();
		this.distances = new CompoundDistances(table, compIds);
		panel.add(new HTML("Loading ..."));
		initWidget(panel);
	}

	@Override
	public void onEvaluate(EvaluationEvent event) {
		panel.clear();

		DataInputPanel input = (DataInputPanel)event.getSource(); // XXX
		distances.calculate(input.getValues());

		Resolver resolver = new Resolver(table);

		final FlexTable flexTable = new FlexTable();
		flexTable.setStylePrimaryName("distances");

		DescriptionColumn description = table.getColumn(DescriptionColumn.class);
		LabelsColumn labels = table.getColumn(LabelsColumn.class);

		PropertyColumn property = table.getColumn(PropertyColumn.class);
		NumberFormat fmt = NumberFormat.getFormat("0.0000");
		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);
		for (int i=0; i<Math.min(5, distances.size()); i++) {
			if (i > 0) {
				int separatorRow = flexTable.getRowCount();
				flexTable.setHTML(separatorRow, 0, "<hr/>");
				flexTable.getFlexCellFormatter().setColSpan(separatorRow, 0, 5);
			}

			String compId = distances.getId(i);
			double distance = distances.getDistance(i);

			final int row = flexTable.getRowCount();

			String depictionUrl = resolver.resolveURL(compId) + "&crop=2";
			ImagePreloader.load(depictionUrl, new ImageLoadHandler() {
				@Override
				public void imageLoaded(ImageLoadEvent event) {
					if (!event.isLoadFailed()) {
						flexTable.setWidget(row, 0, new Image(event.getImageUrl()));
					}
				}
			});

			int spanRows = 4;
			spanRows += hasAttribute(description, compId) ? 1 : 0;
			spanRows += hasAttribute(labels, compId) ? 1 : 0;
			flexTable.getFlexCellFormatter().setRowSpan(row, 0, spanRows);

			flexTable.setText(row, 1, "Id:");
			flexTable.setText(row, 2, compId);

			flexTable.setText(row, 3, "Distance:");
			flexTable.setText(row, 4, fmt.format(distance));

			flexTable.setText(row+1, 0, "Name:");
			flexTable.setText(row+1, 1, resolver.getName(compId));

			flexTable.setText(row+1, 2, "Experimental:");
			flexTable.setText(row+1, 3, fmtParameter(property, compId));

			flexTable.setText(row+2, 0, "CAS:");
			flexTable.setText(row+2, 1, resolver.getCas(compId));

			flexTable.setText(row+2, 2, "Calculated:");

			flexTable.setText(row+3, 0, "Set:");

			for (PredictionColumn p: predictions) {
				if (p.getValues().containsKey(compId)) {
					flexTable.setText(row+2, 3, fmtParameter(p,compId));
					flexTable.setText(row+3, 1, p.getId());
					break;
				}
			}

			spanRows = 4;
			if (hasAttribute(description, compId)) {
				flexTable.setText(row+spanRows, 0, "Description:");
				flexTable.setText(row+spanRows, 1, description.getValue(compId));
				flexTable.getFlexCellFormatter().setColSpan(row+spanRows, 1, 3);
				spanRows++;
			}

			if (hasAttribute(labels, compId)) {
				flexTable.setText(row+spanRows, 0, "Labels:");
				flexTable.setText(row+spanRows, 1, labels.getValue(compId));
				flexTable.getFlexCellFormatter().setColSpan(row+spanRows, 1, 3);
				spanRows++;
			}
		}

		panel.add(flexTable);
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
