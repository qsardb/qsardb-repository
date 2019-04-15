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

class DescriptionLabel extends Composite {
	private final Tooltip tooltip;
	private final PopupHandler handler = new PopupHandler();

	public  DescriptionLabel(Tooltip tooltip) {
		this.tooltip = tooltip;
		init();
	}

	private void init() {
		InlineLabel label = new InlineLabel("i");
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
