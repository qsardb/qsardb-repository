package org.dspace.gwt.client;

import java.util.*;

import org.dspace.gwt.rpc.*;

public class Resolver {

	private QdbTable table = null;


	public Resolver(QdbTable table){
		setTable(table);
	}

	public Map<String, String> resolve(String key){
		Map<String, String> result = new LinkedHashMap<String, String>();
		result.put(ID, key);
		result.put(NAME, getName(key));
		result.put(CAS, getCas(key));
		result.put(INCHI, getInChI(key));
		result.put(SMILES, getSmiles(key));

		return result;
	}

	public String getName(String key){
		return getValue(NameColumn.class, key);
	}

	public String getCas(String key){
		return getValue(CasColumn.class, key);
	}

	public String getInChI(String key){
		return getValue(InChIColumn.class, key);
	}

	public String getSmiles(String key){
		return getValue(SmilesColumn.class, key);
	}

	private String getValue(Class<? extends QdbColumn<?>> clazz, String key){
		QdbColumn<?> column = getTable().getColumn(clazz);

		if(column != null){
			return (String)column.getValue(key);
		}

		return null;
	}

	public QdbTable getTable(){
		return this.table;
	}

	private void setTable(QdbTable table){
		this.table = table;
	}

	public static final String ID = "Id";

	public static final String NAME = "Name";

	public static final String CAS = "Cas";

	public static final String INCHI = "InChI";

	public static final String SMILES = "SMILES";
}