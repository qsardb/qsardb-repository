package org.dspace.qsardb.service.gwt;

import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.server.rpc.*;

import org.dspace.app.util.*;
import org.dspace.core.*;
import org.dspace.qsardb.rpc.gwt.*;
import org.dspace.qsardb.service.*;

abstract
public class DSpaceRemoteServiceServlet extends RemoteServiceServlet {

	private ContextProvider contextProvider = null;

	private ThreadLocal<Context> perThreadContext = new ThreadLocal<Context>(){

		@Override
		protected Context initialValue(){
			throw new UnsupportedOperationException();
		}
	};


	@Override
	public void init() throws ServletException {
		super.init();

		try {
			this.contextProvider = ContextUtil.getContentProvider(getInitParameter("contextProvider"));
		} catch(Exception e){
			throw new ServletException(e);
		}
	}

	@Override
	public String processCall(String payload) throws SerializationException {
		HttpServletRequest request = getThreadLocalRequest();

		try {
			Context context = this.contextProvider.getContext(request);

			this.perThreadContext.set(context);

			try {
				String result = super.processCall(payload);

				context.complete();

				return result;
			} finally {
				this.perThreadContext.remove();

				if(context != null && context.isValid()){
					context.abort();
				}
			}
		} catch(SQLException se){
			return RPC.encodeResponseForFailure(null, new RuntimeException(se.getMessage())); // XXX
		}
	}

	final
	protected Context getThreadLocalContext(){
		return this.perThreadContext.get();
	}

	public DSpaceException formatException(Exception e){
		log(e.getMessage(), e);

		if(e instanceof DSpaceException){
			return (DSpaceException)e;
		}

		return new DSpaceException(e.getMessage());
	}
}