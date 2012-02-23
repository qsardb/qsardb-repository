package org.dspace.gwt.rpc;

import com.google.gwt.user.client.rpc.*;

public interface ExplorerService extends RemoteService {

	ModelTable loadModelTable(String handle, String id) throws DSpaceException;
}