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
	public MathContext getContext(Collection<?> values){
		BigDecimal sum = new BigDecimal(0);

		for(Object value : values){

			if(value instanceof BigDecimal){
				BigDecimal decimal = (BigDecimal)value;

				sum = sum.add(decimal);
			}
		}

		return new MathContext(sum.scale(), RoundingMode.HALF_UP);
	}
}