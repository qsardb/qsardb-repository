package org.dspace.qsardb.rpc.gwt;

public class ValuesColumn extends QdbColumn<Object> {

	private boolean converted = false;

	public boolean isConverted() {
		return this.converted;
	}

	public void setConverted(boolean converted) {
		this.converted = converted;
	}
}
