package org.dspace.qsardb.client.gwt;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;

public class DescriptorValueChangeEvent extends GwtEvent<DescriptorValueChangeEvent.Handler> {

	public interface Handler extends EventHandler {
		void onDescriptorValueChanged(DescriptorValueChangeEvent event);
	}

	@Override
	public void dispatch(DescriptorValueChangeEvent.Handler handler) {
		handler.onDescriptorValueChanged(this);
	}

	@Override
	public Type<DescriptorValueChangeEvent.Handler> getAssociatedType() {
		return DescriptorValueChangeEvent.TYPE;
	}

	public static final Type<DescriptorValueChangeEvent.Handler> TYPE = new Type<>();
}
