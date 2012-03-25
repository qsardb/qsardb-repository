package org.dspace.gwt.rpc;

public class DescriptorColumn extends ParameterColumn {

	private boolean calculable = false;


	public boolean isCalculable(){
		return this.calculable;
	}

	public void setCalculable(boolean calculable){
		this.calculable = calculable;
	}
}