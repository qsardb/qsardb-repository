package org.dspace.gwt.rpc;

public class DescriptorColumn extends ParameterColumn {

	private boolean calculable = false;

	private String format = null;


	public boolean isCalculable(){
		return this.calculable;
	}

	public void setCalculable(boolean calculable){
		this.calculable = calculable;
	}

	public String getFormat(){
		return this.format;
	}

	public void setFormat(String format){
		this.format = format;
	}
}