package org.dspace.qsardb.rpc.gwt;

public class ModelTable extends QdbTable {

	private String id;
	private String name;
	private String description;

	public String getId(){
		return this.id;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getName(){
		return this.name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}