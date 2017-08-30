package org.dspace.qsardb.client.gwt;

import com.google.gwt.event.shared.*;

public interface ExplorerContext {

	HandlerRegistration addSeriesDisplayEventHandler(SeriesDisplayEventHandler handler, boolean notify);
}