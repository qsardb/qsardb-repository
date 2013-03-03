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
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doService(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doService(request, response);
	}

	@Override
	protected QdbCallable<String> createCallable(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
		final
		String body;

		if("POST".equals(request.getMethod())){
			body = readBody(request);
		} else

		{
			body = null;
		}

		final
		String query = request.getQueryString();

		if(body == null){

			if(query == null){
				return null;
			}
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
				String structure;

				Map<String, String> parameters = new LinkedHashMap<String, String>();

				// HTTP POST
				if(body != null){
					structure = body;

					parameters.putAll(parseDescriptors(model.getQdb(), query));
				} else

				// HTTP GET
				{
					int ampersand = query.indexOf('&');

					if(ampersand > -1){
						structure = URLDecoder.decode(query.substring(0, ampersand), "US-ASCII");

						parameters.putAll(parseDescriptors(model.getQdb(), query.substring(ampersand + 1)));
					} else

					{
						structure = query;
					}
				}

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
	private String readBody(HttpServletRequest request) throws IOException {
		String encoding = request.getCharacterEncoding();
		if(encoding == null){
			encoding = "US-ASCII";
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);

		try {
			InputStream is = request.getInputStream();

			try {
				byte[] buffer = new byte[512];

				while(true){
					int count = is.read(buffer);
					if(count < 0){
						break;
					}

					os.write(buffer, 0, count);
				}
			} finally {
				is.close();
			}
		} finally {
			os.close();
		}

		return os.toString(encoding);
	}

	static
	private Map<String, String> parseDescriptors(Qdb qdb, String string) throws UnsupportedEncodingException {
		Map<String, String> result = new LinkedHashMap<String, String>();

		if(string == null || "".equals(string)){
			return result;
		}

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