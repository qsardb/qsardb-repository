package org.dspace.client.gwt;

import com.google.gwt.user.client.ui.*;

public class DataOutputPanel extends Composite implements EvaluationEventHandler {

	private FlexTable table = null;


	public DataOutputPanel(){
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
		String result = event.getResult();

		String[] parts = result.split("=");

		this.table.setHTML(0, 0, parts[0]);

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