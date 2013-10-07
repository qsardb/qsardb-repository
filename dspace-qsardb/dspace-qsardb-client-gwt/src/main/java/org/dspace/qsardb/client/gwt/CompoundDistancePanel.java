package org.dspace.qsardb.client.gwt;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.*;
import java.util.*;
import org.dspace.qsardb.rpc.gwt.*;

public class CompoundDistancePanel extends Composite implements EvaluationEventHandler {
	private final ModelTable table;
	private final CompoundDistances distances;
	private final VerticalPanel panel;

	CompoundDistancePanel(ModelTable table) {
		this.table = table;
		this.panel = new VerticalPanel();
		this.distances = new CompoundDistances(table);
		panel.add(new HTML("Loading ..."));
		initWidget(panel);
	}

	@Override
	public void onEvaluate(EvaluationEvent event) {
		panel.clear();

		DataInputPanel input = (DataInputPanel)event.getSource(); // XXX
		distances.calculate(input.getValues());

		Resolver resolver = new Resolver(table);

		FlexTable flexTable = new FlexTable();
		flexTable.setStylePrimaryName("distances");

		PropertyColumn property = table.getColumn(PropertyColumn.class);
		NumberFormat fmt = NumberFormat.getFormat("0.0000");
		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);
		for (int i=0; i<Math.min(5, distances.size()); i++) {
			String compId = distances.getId(i);
			double distance = distances.getDistance(i);

			int row = flexTable.getRowCount();

			StringBuilder depiction = new StringBuilder();
			depiction.append("<img src=\"");
			depiction.append(resolver.resolveURL(compId));
			depiction.append("&crop=2");
			depiction.append("\">");

			flexTable.setHTML(row, 0, depiction.toString());
			flexTable.getFlexCellFormatter().setRowSpan(row, 0, 4);
			flexTable.getRowFormatter().addStyleName(row, "start");

			flexTable.setText(row, 1, "Id:");
			flexTable.setText(row, 2, compId);

			flexTable.setText(row, 3, "Distance:");
			flexTable.setText(row, 4, fmt.format(distance));

			flexTable.setText(row+1, 0, "Name:");
			flexTable.setText(row+1, 1, resolver.getName(compId));

			flexTable.setText(row+1, 2, "Experimental:");
			flexTable.setText(row+1, 3, property.getValue(compId).toString());

			flexTable.setText(row+2, 0, "CAS:");
			flexTable.setText(row+2, 1, resolver.getCas(compId));

			flexTable.setText(row+2, 2, "Calculated:");

			flexTable.getRowFormatter().addStyleName(row, "start");
			flexTable.setText(row+3, 0, "Set:");
			flexTable.getRowFormatter().addStyleName(row+3, "end");

			for (PredictionColumn p: predictions) {
				if (p.getValues().containsKey(compId)) {
					flexTable.setText(row+2, 3, p.getValue(compId).toString());
					flexTable.setText(row+3, 1, p.getId());
				}
			}

			flexTable.setHTML(row+4, 0, "<hr>");
			flexTable.getFlexCellFormatter().setColSpan(row+4, 0, 5);
		}

		panel.add(flexTable);
	}

}