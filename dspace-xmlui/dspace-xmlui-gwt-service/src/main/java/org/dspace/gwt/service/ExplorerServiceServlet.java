package org.dspace.gwt.service;

import org.dspace.gwt.rpc.*;

public class ExplorerServiceServlet extends ItemServiceServlet implements ExplorerService {

	public String run(){
		return getClass().getName();
	}
}