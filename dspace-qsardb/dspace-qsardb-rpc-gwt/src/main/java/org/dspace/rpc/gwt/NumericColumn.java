package org.dspace.rpc.gwt;

public class NumericColumn extends QdbColumn<Object> {

	private boolean converted = false;


	public boolean isConverted(){
		return this.converted;
	}

	public void setConverted(boolean converted){
		this.converted = converted;
	}
}