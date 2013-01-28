package org.dspace.client.gwt;

import java.util.*;

import com.google.gwt.core.client.*;

import com.googlecode.gflot.client.*;

public class HistogramDataPoint extends DataPoint {

	protected HistogramDataPoint(){
	}

	final
	public List<String> getIds(){
		List<String> result = new ArrayList<String>();

		JsArrayString array = getObject(3);
		for(int i = 0; i < array.length(); i++){
			result.add(array.get(i));
		}

		return result;
	}

	final
	public void setIds(List<String> ids){
		JsArrayString array = createArray().cast();

		for(String id : ids){
			array.push(id);
		}

		set(3, array);
	}

	static
	public HistogramDataPoint create(double x, List<String> ids){
		HistogramDataPoint result = createArray().cast();
		result.setX(x);
		result.setY(ids.size());

		result.setIds(ids);

		return result;
	}
}