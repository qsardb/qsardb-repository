package org.dspace.qsardb.rpc.gwt;

public class DescriptorColumn extends ParameterColumn {

	private boolean calculable = false;

	private String format = null;

	private String application = null;

	private String predictionApplication = null;

	private String units = null;

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getPredictionApplication() {
		return predictionApplication;
	}

	public void setPredictionApplication(String predictionApplication) {
		this.predictionApplication = predictionApplication;
	}

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
