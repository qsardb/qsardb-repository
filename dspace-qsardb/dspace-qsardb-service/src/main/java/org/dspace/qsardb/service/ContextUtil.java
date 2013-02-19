package org.dspace.qsardb.service;

import org.dspace.app.util.*;

public class ContextUtil {

	private ContextUtil(){
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	static
	public ContextProvider getContentProvider(String name) throws Exception {
		Class<? extends ContextProvider> clazz = (Class<? extends ContextProvider>)Class.forName(name);

		return clazz.newInstance();
	}
}