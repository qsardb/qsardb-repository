package org.dspace.rpc.gwt;

import java.math.*;

public class LeverageColumn extends NumericColumn {

	private BigDecimal criticalValue = null;


	public BigDecimal getCriticalValue(){
		return this.criticalValue;
	}

	public void setCriticalValue(BigDecimal criticalValue){
		this.criticalValue = criticalValue;
	}
}