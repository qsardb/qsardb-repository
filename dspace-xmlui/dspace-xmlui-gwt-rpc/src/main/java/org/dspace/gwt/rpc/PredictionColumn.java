package org.dspace.gwt.rpc;

import java.math.*;
import java.util.*;

public class PredictionColumn extends ParameterColumn {

	private String type = null;

	private Map<String, BigDecimal> errors = null;


	public String getType(){
		return this.type;
	}

	public void setType(String type){
		this.type = type;
	}

	public BigDecimal getError(String key){
		return getErrors().get(key);
	}

	public void setError(String key, BigDecimal value){
		getErrors().put(key, value);
	}

	public Map<String, BigDecimal> getErrors(){
		return this.errors;
	}

	public void setErrors(Map<String, BigDecimal> errors){
		this.errors = errors;
	}
}