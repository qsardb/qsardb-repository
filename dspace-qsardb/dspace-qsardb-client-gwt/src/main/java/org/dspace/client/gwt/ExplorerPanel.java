package org.dspace.client.gwt;

import com.google.gwt.user.client.ui.*;

abstract
public class ExplorerPanel extends Composite {

	private ExplorerContext context = null;


	public ExplorerPanel(ExplorerContext context){
		setContext(context);
	}

	public ExplorerContext getContext(){
		return this.context;
	}

	private void setContext(ExplorerContext context){
		this.context = context;
	}
}