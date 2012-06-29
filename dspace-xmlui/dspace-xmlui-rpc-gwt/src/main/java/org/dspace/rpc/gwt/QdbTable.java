package org.dspace.rpc.gwt;

import java.io.*;
import java.util.*;

abstract
public class QdbTable implements Serializable {

	private Set<String> keys = null;

	private List<QdbColumn<?>> columns = null;


	public <C extends QdbColumn<?>> boolean hasColumn(Class<C> clazz){
		return getColumn(clazz) != null;
	}

	public <C extends QdbColumn<?>> C getColumn(Class<C> clazz){
		List<C> result = getAllColumns(clazz);

		if(result.size() == 1){
			return result.get(0);
		}

		return null;
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	public <C extends QdbColumn<?>> List<C> getAllColumns(Class<C> clazz){
		List<C> result = new ArrayList<C>();

		List<QdbColumn<?>> columns = getColumns();
		for(QdbColumn<?> column : columns){

			if((column.getClass()).equals(clazz)){
				result.add((C)column);
			}
		}

		return result;
	}

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