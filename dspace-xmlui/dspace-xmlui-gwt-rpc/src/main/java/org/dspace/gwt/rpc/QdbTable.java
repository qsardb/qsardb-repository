package org.dspace.gwt.rpc;

import java.io.*;
import java.util.*;

abstract
public class QdbTable implements Serializable {

	private Set<String> keys = null;

	private List<QdbColumn<?>> columns = null;


	public Set<String> getKeys(){
		return this.keys;
	}

	public void setKeys(Set<String> keys){
		this.keys = keys;
	}

	public List<QdbColumn<?>> getColumns(){
		return this.columns;
	}

	public void setColumns(List<QdbColumn<?>> columns){
		this.columns = columns;
	}
}