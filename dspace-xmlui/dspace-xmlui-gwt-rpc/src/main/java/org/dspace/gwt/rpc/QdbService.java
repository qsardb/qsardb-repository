package org.dspace.gwt.rpc;

import java.util.*;

import com.google.gwt.user.client.rpc.*;

public interface QdbService extends RemoteService {

	ModelTable loadModelTable(String handle, String modelId) throws DSpaceException;

	String evaluateModel(String handle, String modelId, Map<String, String> parameters) throws DSpaceException;
}