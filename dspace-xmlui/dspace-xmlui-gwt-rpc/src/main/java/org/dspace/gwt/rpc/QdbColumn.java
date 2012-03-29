package org.dspace.gwt.rpc;

import java.io.*;
import java.util.*;

abstract
public class QdbColumn<V> implements Serializable {

	private Map<String, V> values = null;


	public V getValue(String key){
		return this.values.get(key);
	}

	public void setValue(String key, V value){
		this.values.put(key, value);
	}

	public Map<String, V> getValues(){
    	return this.values;
    }

	public void setValues(Map<String, V> values){
    	this.values = values;
    }
}