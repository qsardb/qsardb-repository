package org.dspace.qsardb.rpc.gwt;

import com.google.gwt.user.client.rpc.*;

public interface QdbService extends RemoteService {

	ModelTable loadModelTable(String handle, String modelId) throws DSpaceException;

}