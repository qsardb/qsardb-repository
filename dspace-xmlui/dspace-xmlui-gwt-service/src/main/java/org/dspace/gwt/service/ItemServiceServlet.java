package org.dspace.gwt.service;

import java.sql.*;

import javax.servlet.http.*;

import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.server.rpc.*;

import org.dspace.app.xmlui.utils.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.handle.*;

abstract
public class ItemServiceServlet extends RemoteServiceServlet {

	private ThreadLocal<Context> perThreadContext = new ThreadLocal<Context>(){

		@Override
		protected Context initialValue(){
			throw new UnsupportedOperationException();
		}
	};

	@Override
	@SuppressWarnings (
		value = {"unused"}
	)
	public String processCall(String payload) throws SerializationException {
		HttpServletRequest request = getThreadLocalRequest();
		HttpServletResponse response = getThreadLocalResponse();

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

	static
	protected Item obtainItem(Context context, String handle) throws SQLException {
		DSpaceObject object = HandleManager.resolveToObject(context, handle);

		if(object instanceof Item){
			boolean authorized = AuthorizeManager.authorizeActionBoolean(context, object, Constants.READ);

			if(authorized){
				Item item = (Item)object;

				return item;
			}
		}

		return null;
	}

	final
	protected Context getThreadLocalContext(){
		return this.perThreadContext.get();
	}
}