package org.dspace.service.gwt;

import java.sql.*;

import javax.servlet.http.*;

import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.server.rpc.*;

import org.dspace.app.xmlui.utils.*;
import org.dspace.core.*;

abstract
public class DSpaceRemoteServiceServlet extends RemoteServiceServlet {

	private ThreadLocal<Context> perThreadContext = new ThreadLocal<Context>(){

		@Override
		protected Context initialValue(){
			throw new UnsupportedOperationException();
		}
	};


	@Override
	public String processCall(String payload) throws SerializationException {
		HttpServletRequest request = getThreadLocalRequest();

		try {
			Context context = ContextUtil.obtainContext(request);

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
}