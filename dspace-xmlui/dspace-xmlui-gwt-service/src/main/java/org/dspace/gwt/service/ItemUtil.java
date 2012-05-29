package org.dspace.gwt.service;

import java.sql.*;

import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.handle.*;

public class ItemUtil {

	private ItemUtil(){
	}

	static
	public Item obtainItem(Context context, String handle) throws SQLException {
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
}