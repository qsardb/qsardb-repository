package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

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
	public Bounds bounds(Map<?, ?> map){
		return bounds(map.values());
	}

	static
	public Bounds bounds(Collection<?> values){
		Bounds bounds = new Bounds();

		return bounds(bounds, values);
	}

	static
	public Bounds bounds(Bounds bounds, Map<?, ?> map){
		return bounds(bounds, map.values());
	}

	static
	public Bounds bounds(Bounds bounds, Collection<?> values){

		for(Object value : values){

			if(value instanceof BigDecimal){
				bounds.update((BigDecimal)value);
			}
		}

		return bounds;
	}

	static
	public Bounds symmetricalBounds(Bounds bounds){
		Bounds result = new Bounds();

		BigDecimal max = ((bounds.getMax()).abs()).max((bounds.getMin()).abs());
		result.update(max);

		BigDecimal min = (max).multiply(new BigDecimal(-1));
		result.update(min);

		return result;
	}

	static
	public class Bounds {

		private BigDecimal min = null;

		private BigDecimal max = null;


		private void update(BigDecimal value){

			if(this.min == null || (this.min).compareTo(value) > 0){
				this.min = value;
			} // End if

			if(this.max == null || (this.max).compareTo(value) < 0){
				this.max = value;
			}
		}

		public BigDecimal getDistance(){
			return (getMax()).subtract(getMin());
		}

		public BigDecimal getMin(){
			return this.min;
		}

		public BigDecimal getMax(){
			return this.max;
		}
	}
}