package org.dspace.qsardb.client.gwt;

import com.google.gwt.user.client.ui.Label;
import org.dspace.qsardb.rpc.gwt.ParameterColumn;

public class DescriptionTooltip extends Tooltip {

	public DescriptionTooltip(ParameterColumn parameter){
		setWidget(new Label(parameter.getDescription()));
	}

	@Override
	protected void render() {
		setPopupPositionAndShow(getPositionCallback());
	}
}