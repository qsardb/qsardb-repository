package org.dspace.client.gwt;

import com.google.gwt.dom.client.*;
import com.google.gwt.user.client.ui.*;

public class Heading extends Widget implements HasText {

	public Heading(int n){
		Document document = Document.get();

		setElement(document.createHElement(n));
	}

	public Heading(String text, int n){
		this(n);

		setText(text);
	}

	@Override
	public String getText(){
		Element element = getElement();

		return element.getInnerText();
	}

	@Override
	public void setText(String text){
		Element element = getElement();

		element.setInnerText(text);
	}
}