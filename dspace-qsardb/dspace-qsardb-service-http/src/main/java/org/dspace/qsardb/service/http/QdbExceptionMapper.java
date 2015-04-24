/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.qsardb.service.http;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QdbExceptionMapper implements ExceptionMapper<Exception> {

	@Override
	public Response toResponse(Exception ex) {
		if (ex instanceof WebApplicationException) {
			return ((WebApplicationException)ex).getResponse();
		}
		
		logger.error(ex.getMessage(), ex);
		return Response.status(500).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
	}

	private static final Logger logger = LoggerFactory.getLogger(QdbExceptionMapper.class);
}
