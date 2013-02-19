package org.dspace.rpc.gwt;

import java.io.*;
import java.util.*;

abstract
public class QdbColumn<V> implements Serializable {

	private int length = 0;

	private Map<String, V> values = null;


	public V getValue(String key){
		return this.values.get(key);
	}

	public void setValue(String key, V value){
		this.values.put(key, value);
	}

	public int getLength(){
		return this.length;
	}

	public void setLength(int length){
		this.length = length;
	}

	public Map<String, V> getValues(){
    	return this.values;
    }

	public void setValues(Map<String, V> values){
    	this.values = values;
    }
}