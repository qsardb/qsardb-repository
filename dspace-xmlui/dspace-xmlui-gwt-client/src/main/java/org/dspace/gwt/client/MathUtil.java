package org.dspace.gwt.client;

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
				sum += ((Number)value).doubleValue();

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
				double deviation = (((Number)value).doubleValue() - mean.doubleValue());

				sum += Math.pow(deviation, 2);

				count++;
			}
		}

		double variance = (sum / (count - 1));

		return Double.valueOf(Math.sqrt(variance));
	}
}