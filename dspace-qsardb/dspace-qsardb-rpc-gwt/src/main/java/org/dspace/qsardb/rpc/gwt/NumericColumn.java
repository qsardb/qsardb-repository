package org.dspace.qsardb.rpc.gwt;

public class NumericColumn extends QdbColumn<Object> {

	private boolean converted = false;
	private boolean numeric = false;


	public boolean isConverted(){
		return this.converted;
	}

	public void setConverted(boolean converted){
		this.converted = converted;
	}

	public boolean isNumeric() {
		return this.numeric;
	}

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}
}