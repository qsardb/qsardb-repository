package org.dspace.rpc.gwt;

public class ModelTable extends QdbTable {

	private String id = null;

	private String name = null;


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
}