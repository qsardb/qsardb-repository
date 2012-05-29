package org.dspace.client.gwt;

import java.util.*;

import ca.nanometrics.gflot.client.*;

public class HistogramDataPoint extends DataPoint {

	private List<String> ids = null;


	public HistogramDataPoint(double x, List<String> ids){
		super(x, ids.size());

		setIds(ids);
	}

	public int getHeight(){
		return getIds().size();
	}

	public List<String> getIds(){
		return this.ids;
	}

	private void setIds(List<String> ids){
		this.ids = ids;
	}
}