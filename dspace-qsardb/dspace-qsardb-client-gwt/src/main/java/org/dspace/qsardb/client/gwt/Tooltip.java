package org.dspace.qsardb.client.gwt;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

abstract
public class Tooltip extends PopupPanel {

	private PositionCallback positionCallback;

	private final Timer timer = new Timer(){
		@Override
		public void run(){
			render();
		}
	};

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

	protected abstract void render();

	protected PositionCallback getPositionCallback() {
		return positionCallback;
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

	public void schedule(final int x, final int y) {
		boolean isScheduled = positionCallback != null;

		positionCallback = new PositionCallback() {
			private final int spacing = 5;

			@Override
			public void setPosition(int width, int height) {
				int left = x + this.spacing;
				int top = y + this.spacing;
				if ((top + height) > (Window.getScrollTop() + Window.getClientHeight())) {
					top = Window.getScrollTop() + Window.getClientHeight() - height;
				}
				if (left + width > (Window.getScrollLeft() + Window.getClientWidth())) {
					left = Math.max(x - this.spacing - width, Window.getScrollLeft());
				}
				setPopupPosition(left, top);
			}
		};

		if (!isScheduled) {
			this.timer.schedule(SHOW_DELAY);
		}
	}

	public void cancel(){
		positionCallback = null;
		this.timer.cancel();

		if(isShowing()){
			hide();
		}
	}

	private static final int SHOW_DELAY = 500;
	private static final int HIDE_DELAY = 100;
}
