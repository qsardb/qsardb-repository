package org.dspace.qsardb.client.gwt;

import java.math.*;
import java.util.*;

import org.dspace.qsardb.rpc.gwt.*;

public class ParameterUtil {

	private ParameterUtil(){
	}

	static
	public Map<String, ?> subset(Set<String> keys, Map<String, ?> values){
		Map<String, Object> result = new LinkedHashMap<String, Object>(values);
		(result.keySet()).retainAll(keys);

		return result;
	}

	static
	public void prepareTable(QdbTable table){
		List<QdbColumn<?>> columns = table.getColumns();

		for(QdbColumn<?> column : columns){

			if(column instanceof PropertyColumn){
				ensureConverted((PropertyColumn)column);
			} else

			if(column instanceof PredictionColumn){
				ensureConverted((PredictionColumn)column);
			} else

			if(column instanceof LeverageColumn){
				ensureConverted((LeverageColumn)column);
			} else

			if(column instanceof MahalanobisDistanceColumn){
				ensureConverted((MahalanobisDistanceColumn)column);
			}
		}

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);
		for(PredictionColumn prediction : predictions){
			prediction.setErrors(calculateErrors(prediction, property));
		}
	}

	static
	public void ensureConverted(NumericColumn column){

		if(column.isConverted()){
			return;
		}

		convertParameterColumn(column);
	}

	static
	private void convertParameterColumn(NumericColumn column){
		Map<String, Object> values = column.getValues();

		Collection<Map.Entry<String, Object>> entries = values.entrySet();
		int stringValuesCount = 0;
		for(Map.Entry<String, Object> entry : entries){
			Object value = convertValue(entry.getValue());
			entry.setValue(value);
			if (value instanceof String) {
				stringValuesCount++;
			}
		}

		column.setConverted(true);
		column.setNumeric(stringValuesCount <  entries.size());
	}

	static
	private Object convertValue(Object value){

		if(value instanceof String){
			String string = (String)value;

			if("0".equals(string)){
				return BigDecimal.ZERO;
			} else

			if("1".equals(string)){
				return BigDecimal.ONE;
			}

			try {
				return new BigDecimal(string);
			} catch(NumberFormatException nfe){
				// Ignored
			}
		}

		return value;
	}

	static
	private Map<String, BigDecimal> calculateErrors(PredictionColumn prediction, PropertyColumn property){
		Map<String, BigDecimal> result = new LinkedHashMap<String, BigDecimal>();

		Set<String> ids = new LinkedHashSet<String>((prediction.getValues()).keySet());
		ids.retainAll((property.getValues()).keySet());

		for(String id : ids){
			Object left = prediction.getValue(id);
			Object right = property.getValue(id);

			if(left instanceof BigDecimal && right instanceof BigDecimal){
				BigDecimal error = ((BigDecimal)left).subtract((BigDecimal)right);

				result.put(id, error);
			}
		}

		return result;
	}

	public static final MathContext context = new MathContext(8);
}