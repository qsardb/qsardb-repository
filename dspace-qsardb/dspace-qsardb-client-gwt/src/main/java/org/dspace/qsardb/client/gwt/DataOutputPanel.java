package org.dspace.qsardb.client.gwt;

import com.google.gwt.user.client.ui.*;
import org.dspace.qsardb.rpc.gwt.ModelTable;
import org.dspace.qsardb.rpc.gwt.PropertyColumn;

public class DataOutputPanel extends Composite implements EvaluationEventHandler {

	private final ModelTable modelTable;
	private FlexTable table;


	public DataOutputPanel(ModelTable modelTable){
		this.modelTable = modelTable;

		Panel panel = new FlowPanel();

		this.table = new FlexTable();
		this.table.setHTML(0, 0, "Loading..");

		FlexTable.ColumnFormatter formatter = this.table.getColumnFormatter();

		formatter.setWidth(0, "30%");
		formatter.setWidth(1, "5%");
		formatter.setWidth(2, "65%");

		panel.add(this.table);

		initWidget(panel);
	}

	@Override
	public void onEvaluate(EvaluationEvent event){
		String result = event.getResponse().getResult();

		String[] parts = result.split("=");

		PropertyColumn property = modelTable.getColumn(PropertyColumn.class);
		FlowPanel propertyPanel = new FlowPanel();

		String propertyUnits = event.getResponse().getResultUnits();
		if (propertyUnits != null && !propertyUnits.trim().equals("")) {
			propertyPanel.add(new InlineLabel(parts[0] + " [" + propertyUnits + "]"));
		} else {
			propertyPanel.add(new InlineLabel(parts[0]));
		}

		if (property.getDescription() != null){
			propertyPanel.add(new DescriptionLabel(property));
		}

		this.table.setWidget(0, 0, propertyPanel);

		FlexTable.RowFormatter formatter = this.table.getRowFormatter();

		for(int i = 1; i < parts.length; i++){
			int row = (i - 1);

			if(i == (parts.length - 1)){
				parts[i] = ("<b>" + parts[i] + "</b>");
			}

			this.table.setHTML(row, 1, "=");
			this.table.setHTML(row, 2, parts[i]);

			formatter.setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
		}
	}
}
