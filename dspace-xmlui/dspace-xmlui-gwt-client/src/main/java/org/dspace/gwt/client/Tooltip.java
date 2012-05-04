package org.dspace.gwt.client;

import com.google.gwt.user.client.ui.*;

abstract
public class Tooltip extends PopupPanel {

	public Tooltip(){
		super(true);
	}

	public static final int TIMER_DELAY = 500;
}