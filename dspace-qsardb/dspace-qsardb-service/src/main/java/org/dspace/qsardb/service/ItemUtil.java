package org.dspace.qsardb.service;

import java.sql.*;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;

public class ItemUtil {

	private ItemUtil(){
	}

	static
	public Item obtainItem(Context context, String handle) throws SQLException {
		HandleService handleService = HandleServiceFactory.getInstance().getHandleService();
		DSpaceObject object = handleService.resolveToObject(context, handle);

		if(object instanceof Item){
			AuthorizeService authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
			boolean authorized = authorizeService.authorizeActionBoolean(context, object, Constants.READ);

			if(authorized){
				Item item = (Item)object;

				return item;
			}
		}

		return null;
	}
}