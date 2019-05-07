package org.dspace.qsardb.rpc.gwt;

public class PropertyColumn extends ParameterColumn {

	private String format = null;
	private boolean regression;

	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public boolean isRegression() {
		return this.regression;
	}

	public void setRegression(boolean value) {
		this.regression = value;
	}
}
