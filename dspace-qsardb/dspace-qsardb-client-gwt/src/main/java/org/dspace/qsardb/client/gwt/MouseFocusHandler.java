package org.dspace.qsardb.client.gwt;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;

abstract
public class MouseFocusHandler implements MouseOverHandler, MouseOutHandler {

	abstract
	public void focusChanged(boolean focus);

	public <W extends UIObject & HasAllMouseHandlers> void install(W widget){
		widget.addMouseOverHandler(this);
		widget.addMouseOutHandler(this);
	}

	@Override
	public void onMouseOver(MouseOverEvent event){
		focusChanged(true);
	}

	@Override
	public void onMouseOut(MouseOutEvent event){
		focusChanged(false);
	}
}