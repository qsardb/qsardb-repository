package org.dspace.service.http;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.*;

import org.dspace.content.*;
import org.dspace.service.*;

import org.qsardb.model.*;

public class PredictorServiceServlet extends ItemServlet {

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		doService(request, response);
	}

	@Override
	protected QdbCallable<String> createCallable(HttpServletRequest request, HttpServletResponse response, String path){
		final
		String query = request.getQueryString();

		if(query == null){
			return null;
		}

		QdbContentCallable<String> callable = new QdbContentCallable<String>(path){

			@Override
			public String call(Object object) throws Exception {

				if(object instanceof Model){
					return call((Model)object);
				}

				throw new IllegalArgumentException();
			}

			private String call(Model model) throws Exception {
				Map<String, String> parameters = new LinkedHashMap<String, String>();

				String structure = query;

				int ampersand = query.indexOf('&');
				if(ampersand > -1){
					parameters.putAll(parseDescriptors(model.getQdb(), query.substring(ampersand + 1)));

					structure = query.substring(0, ampersand);
				}

				structure = URLDecoder.decode(structure, "US-ASCII");

				return PredictorUtil.evaluate(model, parameters, structure);
			}
		};

		return callable;
	}

	@Override
	protected boolean validatePath(String path){
		return path != null && (path.startsWith("models/") || path.contains("/models/"));
	}

	static
	private Map<String, String> parseDescriptors(Qdb qdb, String string) throws UnsupportedEncodingException {
		Map<String, String> result = new LinkedHashMap<String, String>();

		String[] entries = string.split("&");
		for(String entry : entries){
			int equals = entry.indexOf('=');

			String key = URLDecoder.decode(entry.substring(0, equals), "US-ASCII");
			String value = URLDecoder.decode(entry.substring(equals + 1), "US-ASCII");

			Descriptor descriptor = qdb.getDescriptor(key);
			if(descriptor != null){
				result.put(key, value);
			}
		}

		return result;
	}
}