package org.dspace.qsardb.client.gwt;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class CompoundBrowseEvent extends GwtEvent<CompoundBrowseEvent.Handler>{

	public interface Handler extends EventHandler {
		public void onEvent(CompoundBrowseEvent aThis);
	}

	public static Type<Handler> TYPE = new Type<Handler>();

	public final String compoundId;

	CompoundBrowseEvent(String compoundId) {
		this.compoundId = compoundId;
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onEvent(this);
	}
}
