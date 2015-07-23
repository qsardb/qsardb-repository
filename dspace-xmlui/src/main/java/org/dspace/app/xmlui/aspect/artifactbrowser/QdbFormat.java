/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.TextContainer;
import org.dspace.content.QdbParameterUtil;
import org.qsardb.model.Container;
import org.qsardb.model.Parameter;

public class QdbFormat {

	public static void descriptionAttribute(Container container, TextContainer target) throws WingException {
		String description = container.getDescription();
		if (description != null && !description.trim().isEmpty()) {
			target.addInfo("i", description);
		}
	}

	public static void unit(Parameter parameter, TextContainer target) throws WingException {
		String unit = QdbParameterUtil.loadUnits(parameter);
		if (unit != null) {
			target.addContent(" ["+unit+"]");
		}
	}

}
