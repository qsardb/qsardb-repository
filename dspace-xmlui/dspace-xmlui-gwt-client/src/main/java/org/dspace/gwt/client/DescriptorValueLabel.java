package org.dspace.gwt.client;

import com.google.gwt.user.client.ui.*;

public class DescriptorValueLabel extends Label {

	public void setUserValue(Number value){
		setText(value.toString());
	}
}