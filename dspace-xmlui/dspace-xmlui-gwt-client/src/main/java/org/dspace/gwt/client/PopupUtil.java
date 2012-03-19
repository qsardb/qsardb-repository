package org.dspace.gwt.client;

import com.google.gwt.dom.client.*;

public class PopupUtil {

	private PopupUtil(){
	}

	static
	public int getPopupX(NativeEvent event){
		Document document = Document.get();

		return event.getClientX() + document.getScrollLeft();
	}

	static
	public int getPopupY(NativeEvent event){
		Document document = Document.get();

		return event.getClientY() + document.getScrollTop();
	}
}