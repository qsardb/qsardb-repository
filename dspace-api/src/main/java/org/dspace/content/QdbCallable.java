package org.dspace.content;

import org.qsardb.model.*;

public interface QdbCallable<X> {

	X call(Qdb qdb) throws Exception;
}