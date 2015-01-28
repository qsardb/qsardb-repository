package org.dspace.qsardb.client.gwt;

import com.google.gwt.user.client.ui.Label;
import org.dspace.qsardb.rpc.gwt.ModelTable;
import org.dspace.qsardb.rpc.gwt.ParameterColumn;

public class DescriptionTooltip extends Tooltip {

	public DescriptionTooltip(ParameterColumn parameter){
		setDescription(parameter.getDescription());
	}

	public DescriptionTooltip(ModelTable table) {
		setDescription(table.getDescription());
	}

	private void setDescription(String description) {
		Label label = new Label(description);
		label.setStylePrimaryName("description-tooltip");
		setWidget(label);
	}

	@Override
	protected void render() {
		setPopupPositionAndShow(getPositionCallback());
	}
}