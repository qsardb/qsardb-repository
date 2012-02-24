package org.dspace.gwt.client;

import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

public class CompoundSimplePager extends SimplePager {

	public CompoundSimplePager(){
		super();

		adjustCellAlignment();
	}

	private void adjustCellAlignment(){
		HorizontalPanel panel = (HorizontalPanel)getWidget();

		if(panel.getWidgetCount() != alignment.length){
			throw new IllegalStateException();
		}

		for(int i = 0; i < alignment.length; i++){
			Widget widget = panel.getWidget(i);

			Element td = DOM.getParent(widget.getElement());
			DOM.setElementAttribute(td, "align", alignment[i]);
		}
	}

	private static final String[] alignment = {"left", "left", "center", "right", "right"};
}