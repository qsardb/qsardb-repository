package org.dspace.client.gwt;

import com.google.gwt.user.client.ui.*;

abstract
public class Tooltip extends PopupPanel {

	public Tooltip(){
		super(true);
	}

	public static final int TIMER_DELAY = 500;
}