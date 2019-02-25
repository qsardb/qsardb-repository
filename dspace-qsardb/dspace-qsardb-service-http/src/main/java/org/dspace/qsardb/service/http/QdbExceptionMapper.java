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
		Error error = new Error();
		error.errorMessage = ex.getMessage();

		if (ex instanceof WebApplicationException) {
			Response orig = ((WebApplicationException)ex).getResponse();
			return makeResponse(orig.getStatus(), error);
		}

		if (ex instanceof IllegalArgumentException) {
			return makeResponse(400, error);
		}
		
		logger.error(ex.getMessage(), ex);
		return makeResponse(500, error);
	}

	private Response makeResponse(int statusCode, Error error) {
		return Response
				.status(statusCode)
				.entity(error)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}

	public static class Error {
		String errorMessage;

		public String getErrorMessage() {
			return errorMessage;
		}
	}


	private static final Logger logger = LoggerFactory.getLogger(QdbExceptionMapper.class);
}
