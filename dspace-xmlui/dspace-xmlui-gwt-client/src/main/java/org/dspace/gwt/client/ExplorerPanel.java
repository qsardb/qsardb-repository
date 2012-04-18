package org.dspace.gwt.client;

import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.ui.*;

abstract
public class ExplorerPanel extends Composite implements SeriesDisplayEventHandler {

	@Override
	public void onVisibilityChanged(SeriesDisplayEvent event){
		fireEvent(event);
	}

	public HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler){
		return addHandler(handler, SeriesDisplayEvent.TYPE);
	}
}