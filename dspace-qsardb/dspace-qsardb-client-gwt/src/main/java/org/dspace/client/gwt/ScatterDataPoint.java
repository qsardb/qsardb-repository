package org.dspace.client.gwt;

import com.googlecode.gflot.client.*;

public class ScatterDataPoint extends DataPoint {

	protected ScatterDataPoint(){
	}

	final
	public String getId(){
		return getString(3);
	}

	final
	public void setId(String id){
		set(3, id);
	}

	static
	public ScatterDataPoint create(double x, double y, String id){
		ScatterDataPoint result = createArray().cast();
		result.setX(x);
		result.setY(y);

		result.setId(id);

		return result;
	}
}