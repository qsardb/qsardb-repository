package org.dspace.service.http;

import java.io.*;
import java.sql.*;
import java.util.regex.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;

import org.dspace.app.xmlui.utils.*;
import org.dspace.core.*;

abstract
public class DSpaceHttpServlet extends HttpServlet {

	private ThreadLocal<Context> perThreadContext = new ThreadLocal<Context>(){

		@Override
		protected Context initialValue(){
			throw new UnsupportedOperationException();
		}
	};


	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();

		try {
			Context context = ContextUtil.obtainContext(request);

			logger.debug(session.getId() + ": obtained context " + context.toString());

			this.perThreadContext.set(context);

			try {
				super.service(request, response);

				logger.debug(session.getId() + ": successfully completing context " + context.toString());

				context.complete();
			} finally {
				this.perThreadContext.remove();

				if(context != null && context.isValid()){
					logger.debug(session.getId() + ": aborting context " + context.toString());

					context.abort();
				}
			}
		} catch(SQLException se){
			throw new ServletException(se);
		}
	}

	final
	protected Context getThreadLocalContext(){
		return this.perThreadContext.get();
	}

	static
	protected Matcher createMatcher(HttpServletRequest request){
		Pattern pattern = Pattern.compile(request.getContextPath() + request.getServletPath() + "/(\\d+/\\d+)/(.*)");

		return pattern.matcher(request.getRequestURI());
	}

	private static final Logger logger = Logger.getLogger(DSpaceHttpServlet.class);
}