package org.dspace.rpc.gwt;

import java.math.*;
import java.util.*;

public class PredictionColumn extends ParameterColumn {

	private Type type = null;

	private Map<String, BigDecimal> errors = null;


	public Type getType(){
		return this.type;
	}

	public void setType(Type type){
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

	static
	public enum Type {
		TRAINING,
		VALIDATION,
		TESTING,
		;
	}
}