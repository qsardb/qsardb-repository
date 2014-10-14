package org.dspace.qsardb.client.gwt;

import java.util.*;

import org.dspace.qsardb.rpc.gwt.*;

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

	public String resolveMethod(Map<String, String> values) {
		for (String method: resolveOrder) {
			if (values.get(method) != null) {
				return method;
			}
		}
		return null;
	}

	public String resolveURL(Map<String, String> values) {
		String structure = values.get(resolveMethod(values));
		StringBuilder sb = new StringBuilder();
		sb.append("http://cactus.nci.nih.gov/chemical/structure/");
		sb.append(structure.replace("#", "%23").replace("?", "%3f"));
		sb.append("/image").append("?format=png");

		return sb.toString();
	}

	public String resolveURL(String key) {
		return resolveURL(resolve(key));
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

	public static final String CAS = "CAS";

	public static final String INCHI = "InChI";

	public static final String SMILES = "SMILES";

	private static final String resolveOrder[] = { INCHI, SMILES, NAME };
}