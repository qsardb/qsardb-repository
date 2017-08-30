package org.dspace.qsardb.rpc.gwt;

public class PropertyColumn extends ParameterColumn {

	private String format = null;


	public String getFormat(){
		return this.format;
	}

	public void setFormat(String format){
		this.format = format;
	}
}