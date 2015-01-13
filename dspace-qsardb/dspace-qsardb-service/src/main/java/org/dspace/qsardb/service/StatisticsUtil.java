/*
 * Copyright (c) 2014 University of Tartu
 */
package org.dspace.qsardb.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.model.Parameter;

import org.apache.commons.math.stat.descriptive.*;
import org.apache.commons.math.stat.regression.*;
import org.qsardb.cargo.map.BigDecimalFormat;

public class StatisticsUtil {

	public static Values<?> loadValues(Parameter<?, ?> parameter) throws IOException {
		if (parameter.hasCargo(ValuesCargo.class)) {
			ValuesCargo valuesCargo = parameter.getCargo(ValuesCargo.class);
			try {
				FlexBigDecimalFormat valueFormat = new FlexBigDecimalFormat();
				Map<String, BigDecimal> values = valuesCargo.loadMap(valueFormat);
				return new BigDecimalValues(values);
			} catch (Exception e) {
				// Ignored
			}
			return new StringValues(valuesCargo.loadStringMap());
		}
		return new StringValues(Collections.<String, String>emptyMap());
	}
	
	public static abstract class Values<X> {
		private Map<String, X> values = null;

		private Values(Map<String, X> values){
			this.values = values;
		}

		public int size(){
			return this.values.size();
		}

		public BigDecimal rsq(Values<?> values){
			return null;
		}

		public BigDecimal stdev(Values<?> values){
			return null;
		}

		public BigDecimal accuracy(Values<?> values){
			return null;
		}

		public X get(String key){
			return this.values.get(key);
		}

		public Set<String> keySet(){
			return this.values.keySet();
		}
	}

	public static class StringValues extends Values<String> {

		public StringValues(Map<String, String> values){
			super(values);
		}

		@Override
		public BigDecimal accuracy(Values<?> values) {
			StringValues that = (StringValues)values;
			int correct = 0;
			int total = 0;
			for(String key : keySet()){
				String thisValue = this.get(key);
				Object thatValue = that.get(key);

				if(thisValue == null || thatValue == null){
					continue;
				}

				if (thisValue.equals(thatValue)) {
					correct++;
				}
				total++;
			}
			return new BigDecimal(correct).divide(new BigDecimal(total), 2, RoundingMode.HALF_UP);
		}
	}

	public static class BigDecimalValues extends Values<BigDecimal> {

		public BigDecimalValues(Map<String, BigDecimal> values){
			super(values);
		}

		@Override
		public BigDecimal rsq(Values<?> values){

			if(values instanceof BigDecimalValues){
				BigDecimalValues that = (BigDecimalValues)values;

				SimpleRegression regression = new SimpleRegression();

				Set<String> keys = new LinkedHashSet<String>(this.keySet());
				keys.retainAll(that.keySet());

				for(String key : keys){
					BigDecimal thisValue = this.get(key);
					BigDecimal thatValue = that.get(key);

					if(thisValue == null || thatValue == null){
						continue;
					}

					regression.addData(thisValue.doubleValue(), thatValue.doubleValue());
				}

				if (regression.getN() < 2) {
					return null;
				}

				BigDecimal result = new BigDecimal(regression.getRSquare());
				result = result.setScale(3, RoundingMode.HALF_UP);

				return result;
			}

			return super.rsq(values);
		}

		@Override
		public BigDecimal stdev(Values<?> values){

			if(values instanceof BigDecimalValues){
				BigDecimalValues that = (BigDecimalValues)values;

				DescriptiveStatistics statistic = new DescriptiveStatistics();

				Set<String> keys = new LinkedHashSet<String>(this.keySet());
				keys.retainAll(that.keySet());

				for(String key : keys){
					BigDecimal thisValue = this.get(key);
					BigDecimal thatValue = that.get(key);

					if(thisValue == null || thatValue == null){
						continue;
					}

					statistic.addValue((thisValue).subtract(thatValue).doubleValue());
				}

				if (statistic.getN() < 2) {
					return null;
				}

				BigDecimal result = new BigDecimal(statistic.getStandardDeviation());
				result = result.setScale(3, RoundingMode.HALF_UP);

				return result;
			}

			return super.stdev(values);
		}
	}

	private static class FlexBigDecimalFormat extends BigDecimalFormat {

		@Override
		public BigDecimal parseString(String string){
			if(isText(string)){
				return null;
			}

			return super.parseString(string);
		}

		private static boolean isText(String string){
			for(int i = 0; string != null && i < string.length(); i++){
				char c = string.charAt(i);

				if(!Character.isLetter(c)){
					return false;
				}
			}

			return true;
		}
	}
}
