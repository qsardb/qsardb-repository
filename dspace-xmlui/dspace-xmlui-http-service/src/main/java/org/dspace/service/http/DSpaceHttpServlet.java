package org.dspace.service.http;

import java.io.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

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

		try {
			Context context = ContextUtil.obtainContext(request);

			this.perThreadContext.set(context);

			try {
				super.service(request, response);

				context.complete();
			} finally {
				this.perThreadContext.remove();

				if(context != null && context.isValid()){
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
}