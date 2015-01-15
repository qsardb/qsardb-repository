/*
 * Copyright (c) 2014 University of Tartu
 */
package org.dspace.qsardb.client.gwt;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import org.dspace.qsardb.rpc.gwt.ParameterColumn;

class DescriptionLabel extends Composite {
	private final DescriptionTooltip tooltip;
	private final PopupHandler handler = new PopupHandler();

	public DescriptionLabel(ParameterColumn parameter) {
		tooltip = new DescriptionTooltip(parameter);
		InlineLabel label = new InlineLabel("Description");
		label.setStylePrimaryName("info-label");
		label.addMouseOverHandler(handler);
		label.addMouseOutHandler(handler);
		initWidget(label);
	}

	private class PopupHandler implements MouseOverHandler, MouseOutHandler {

		@Override
		public void onMouseOver(MouseOverEvent event) {
			NativeEvent e = event.getNativeEvent();
			tooltip.schedule(PopupUtil.getPopupX(e), PopupUtil.getPopupY(e));
		}

		@Override
		public void onMouseOut(MouseOutEvent event) {
			tooltip.cancel();
		}
	}
	
}
