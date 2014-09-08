package org.dspace.qsardb.client.gwt;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;

abstract
public class Tooltip extends PopupPanel {

	private final Timer hideTimer = new Timer() {
		@Override
		public void run(){
			Tooltip.super.hide();
		}
	};

	public Tooltip(){
		super(true);
		sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
	}

	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);
		switch (event.getTypeInt()) {
			case Event.ONMOUSEOVER:
				hideTimer.cancel();
				break;
			case Event.ONMOUSEOUT:
				hideTimer.schedule(HIDE_DELAY);
				break;
		}
	}

	@Override
	public void hide() {
		hideTimer.schedule(HIDE_DELAY);
	}

	public static final int SHOW_DELAY = 500;
	private static final int HIDE_DELAY = 100;
}
