package org.dspace.rpc.gwt;

abstract
public class ParameterColumn extends QdbColumn<Object> {

	private String id = null;

	private String name = null;

	private boolean converted = false;


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

	public boolean isConverted(){
		return this.converted;
	}

	public void setConverted(boolean converted){
		this.converted = converted;
	}
}