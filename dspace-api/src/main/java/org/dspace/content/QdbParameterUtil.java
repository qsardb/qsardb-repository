/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.content;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.cargo.ucum.UCUMCargo;
import org.qsardb.model.Parameter;

public class QdbParameterUtil {

	public static String loadUnits(Parameter parameter) {
		if (parameter != null && parameter.hasCargo(UCUMCargo.class)) {
			try {
				return parameter.getCargo(UCUMCargo.class).loadString().trim();
			} catch (IOException ignore) {
			}
		}
		return "";
	}

	public static Map<String, String> loadStringValues(Parameter<?, ?> parameter) {
		if (parameter != null && parameter.hasCargo(ValuesCargo.class)) {
			try {
				ValuesCargo cargo = parameter.getCargo(ValuesCargo.class);
				return cargo.loadStringMap();
			} catch (IOException ignore) {
			}
		}
		return Collections.emptyMap();
	}

	public static Map<String, Double> loadDoubleValues(Parameter<?, ?> parameter) {
		if (parameter != null && parameter.hasCargo(ValuesCargo.class)) {
			try {
				ValuesCargo cargo = parameter.getCargo(ValuesCargo.class);
				return cargo.loadDoubleMap();
			} catch (IOException ignore) {
			}
		}
		return Collections.emptyMap();
	}
}
