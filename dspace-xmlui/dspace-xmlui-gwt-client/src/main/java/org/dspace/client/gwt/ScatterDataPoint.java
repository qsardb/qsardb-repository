package org.dspace.client.gwt;

import ca.nanometrics.gflot.client.*;

public class ScatterDataPoint extends DataPoint {

	private String id = null;


	public ScatterDataPoint(double x, double y, String id){
		super(x, y);

		setId(id);
	}

	public String getId(){
		return this.id;
	}

	private void setId(String id){
		this.id = id;
	}
}