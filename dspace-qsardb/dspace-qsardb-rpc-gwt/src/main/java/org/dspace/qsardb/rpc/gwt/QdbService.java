package org.dspace.qsardb.rpc.gwt;

import java.util.*;

import com.google.gwt.user.client.rpc.*;

public interface QdbService extends RemoteService {

	ModelTable loadModelTable(String handle, String modelId) throws DSpaceException;

	Map<String, String> calculateModelDescriptors(String handle, String modelId, String string) throws DSpaceException;

	String evaluateModel(String handle, String modelId, Map<String, String> parameters) throws DSpaceException;
}