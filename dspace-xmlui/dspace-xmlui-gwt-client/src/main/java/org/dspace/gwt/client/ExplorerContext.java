package org.dspace.gwt.client;

import com.google.gwt.event.shared.*;

public interface ExplorerContext {

	HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler, boolean notify);
}