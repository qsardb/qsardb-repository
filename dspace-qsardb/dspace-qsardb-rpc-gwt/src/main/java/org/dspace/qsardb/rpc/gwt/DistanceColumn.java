package org.dspace.qsardb.rpc.gwt;

import java.math.*;

abstract
public class DistanceColumn extends NumericColumn {

	private BigDecimal criticalValue = null;


	abstract
	public String getName();

	public BigDecimal getCriticalValue(){
		return this.criticalValue;
	}

	public void setCriticalValue(BigDecimal criticalValue){
		this.criticalValue = criticalValue;
	}
}