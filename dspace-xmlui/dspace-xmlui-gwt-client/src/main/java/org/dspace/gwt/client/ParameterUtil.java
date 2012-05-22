package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import org.dspace.gwt.rpc.*;

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
	public void convertTable(QdbTable table){
		List<QdbColumn<?>> columns = table.getColumns();

		for(QdbColumn<?> column : columns){

			if(column instanceof ParameterColumn){
				convertParameterColumn((ParameterColumn)column);
			}
		}

		PropertyColumn property = table.getColumn(PropertyColumn.class);

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);
		for(PredictionColumn prediction : predictions){
			prediction.setErrors(calculateErrors(prediction, property));
		}
	}

	static
	private void convertParameterColumn(ParameterColumn column){
		Map<String, Object> values = column.getValues();

		Collection<Map.Entry<String, Object>> entries = values.entrySet();
		for(Map.Entry<String, Object> entry : entries){
			Object value = entry.getValue();

			try {
				if(value instanceof String){
					String string = (String)value;

					if("0".equals(string)){
						entry.setValue(BigDecimal.ZERO);
					} else

					if("1".equals(string)){
						entry.setValue(BigDecimal.ONE);
					} else

					{
						entry.setValue(new BigDecimal(string));
					}
				}
			} catch(NumberFormatException nfe){
				// Ignored
			}
		}
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
}