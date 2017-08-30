package org.dspace.qsardb.client.gwt;

import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

public class CompoundSimplePager extends SimplePager {

	public CompoundSimplePager(){
		super(TextLocation.CENTER, false, true);

		adjustCellAlignment();
	}

	private void adjustCellAlignment(){
		HorizontalPanel panel = (HorizontalPanel)getWidget();

		if(panel.getWidgetCount() != 5){
			throw new IllegalStateException();
		}

		for(int i = 0; i < 5; i++){
			Widget widget = panel.getWidget(i);

			Element td = DOM.getParent(widget.getElement());

			setAttribute(td, "align", alignments[i]);
			setAttribute(td, "valign", "middle");

			setAttribute(td, "width", widths[i]);
		}
	}

	static
	private void setAttribute(Element element, String name, String value){

		if(value != null){
			DOM.setElementAttribute(element, name, value);
		}
	}

	private static final String[] alignments = {"left", "left", "center", "right", "right"};

	private static final String[] widths = {"30px", "30px", null, "30px", "30px"};
}