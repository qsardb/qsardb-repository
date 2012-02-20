package org.dspace.gwt.service;

import org.dspace.gwt.rpc.*;

public class PredictorServiceServlet extends ItemServiceServlet implements PredictorService {

	public String run(){
		return getClass().getName();
	}
}