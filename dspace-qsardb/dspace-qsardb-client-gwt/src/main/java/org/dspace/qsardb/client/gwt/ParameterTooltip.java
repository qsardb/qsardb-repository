package org.dspace.qsardb.client.gwt;

import com.google.gwt.user.client.ui.Label;
import org.dspace.qsardb.rpc.gwt.ParameterColumn;

public class ParameterTooltip extends Tooltip {

	public ParameterTooltip(ParameterColumn parameter){
		setWidget(new Label(parameter.getName()));
	}

	@Override
	protected void render() {
		setPopupPositionAndShow(getPositionCallback());
	}
}