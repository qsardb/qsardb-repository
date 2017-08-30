/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.qsardb.service;

import java.sql.SQLException;
import org.dspace.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QdbContext {
	private static final Logger log = LoggerFactory.getLogger(QdbContext.class);
	private static final ThreadLocal<Context> instance = new ThreadLocal<Context>();

	private QdbContext() {
	}

	public static void init(Context context) {
		instance.set(context);
		log.debug("{}: obtained context {}", Thread.currentThread(), context);
	}

	public static Context getContext() {
		return instance.get();
	}

	public static void complete() {
		Context context = getContext();
		if (context != null) {
			instance.remove();
			try {
				log.debug("{}: completing context {}", Thread.currentThread(), context);
				context.complete();
			} catch (SQLException ex) {
				throw new RuntimeException("Unable to complete context", ex);
			}
		}
	}

	public static void abort() {
		Context context = getContext();
		if (context != null) {
			instance.remove();
			if (context.isValid()) {
				log.debug("{}: aborting context {}", Thread.currentThread(), context);
				context.abort();
			}
		}
	}
}
