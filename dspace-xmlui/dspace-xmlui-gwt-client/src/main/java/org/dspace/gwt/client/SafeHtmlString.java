package org.dspace.gwt.client;

import com.google.gwt.safehtml.shared.*;

class SafeHtmlString implements SafeHtml {

	private String html = null;


	public SafeHtmlString(String html){
		this.html = html;
	}

	@Override
	public String asString(){
		return this.html;
	}

	@Override
	public int hashCode(){
		return this.html.hashCode();
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof SafeHtmlString){
			SafeHtmlString that = (SafeHtmlString)object;

			return (this.html).equals(that.html);
		}

		return false;
	}
}