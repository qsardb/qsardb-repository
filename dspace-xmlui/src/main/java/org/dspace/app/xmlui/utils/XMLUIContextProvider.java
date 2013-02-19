package org.dspace.app.xmlui.utils;

import java.sql.*;

import javax.servlet.http.*;

import org.dspace.app.util.*;
import org.dspace.core.*;

public class XMLUIContextProvider implements ContextProvider {

	@Override
	public Context getContext(HttpServletRequest request) throws SQLException {
		return ContextUtil.obtainContext(request);
	}
}