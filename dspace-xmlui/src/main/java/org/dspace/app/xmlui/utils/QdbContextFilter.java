/*
 *  Copyright (c) 2015 University of Tartu
 */
package org.dspace.app.xmlui.utils;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.dspace.qsardb.service.QdbContext;

public class QdbContextFilter implements Filter {

	@Override
	public void init(FilterConfig fc) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		try {
			QdbContext.init(ContextUtil.obtainContext(httpRequest));
			fc.doFilter(request, response);
			QdbContext.complete();
		} catch (SQLException ex) {
			throw new ServletException(ex);
		} finally {
			QdbContext.abort();
		}
	}

	@Override
	public void destroy() {
	}
}
