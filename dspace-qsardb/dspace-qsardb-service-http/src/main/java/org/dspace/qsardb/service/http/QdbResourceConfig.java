/*
 *  Copyright (c) 2015 University of Tartu
 */
package org.dspace.qsardb.service.http;

import org.glassfish.jersey.server.ResourceConfig;

public class QdbResourceConfig extends ResourceConfig {

	public QdbResourceConfig() {
		register(QdbExceptionMapper.class);

		registerClasses(PredictorResource.class);
	}
}
