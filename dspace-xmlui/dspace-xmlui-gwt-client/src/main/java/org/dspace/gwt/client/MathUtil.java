package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

public class MathUtil {

	private MathUtil(){
	}

	static
	public Double mean(Collection<?> values){
		double sum = 0;

		int count = 0;

		for(Object value : values){

			if(value instanceof Number){
				Number number = (Number)value;

				sum += number.doubleValue();

				count++;
			}
		}

		return Double.valueOf(sum / count);
	}

	static
	public Double standardDeviation(Collection<?> values){
		double sum = 0;

		int count = 0;

		Double mean = mean(values);

		for(Object value : values){

			if(value instanceof Number){
				Number number = (Number)value;

				double deviation = (number.doubleValue() - mean.doubleValue());

				sum += Math.pow(deviation, 2);

				count++;
			}
		}

		double variance = (sum / (count - 1));

		return Double.valueOf(Math.sqrt(variance));
	}

	static
	public int getScale(String format){
		int dot = format.lastIndexOf('.');

		if(dot > -1){
			return (format.length() - (dot + 1));
		}

		return 0;
	}

	static
	public MathContext getContext(Collection<?> values){
		return getContext(values, -1);
	}

	static
	public MathContext getContext(Collection<?> values, int maxPrecision){
		int precision = 0;

		for(Object value : values){

			if(value instanceof BigDecimal){
				BigDecimal decimal = (BigDecimal)value;

				precision = Math.max(decimal.precision(), precision);
			}
		}

		if(maxPrecision > -1){
			precision = Math.min(maxPrecision, precision);
		}

		return new MathContext(precision, RoundingMode.HALF_UP);
	}
}