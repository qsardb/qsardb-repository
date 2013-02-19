package org.dspace.app.util;

import java.sql.*;

import javax.servlet.http.*;

import org.dspace.core.*;

public interface ContextProvider {

	Context getContext(HttpServletRequest request) throws SQLException;
}