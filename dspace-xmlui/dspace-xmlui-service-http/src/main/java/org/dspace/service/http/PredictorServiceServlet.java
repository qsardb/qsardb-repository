package org.dspace.service.http;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import javax.servlet.http.*;

import org.qsardb.model.*;

import org.apache.log4j.*;

import org.dspace.content.*;
import org.dspace.content.QdbUtil;
import org.dspace.core.*;
import org.dspace.service.*;

public class PredictorServiceServlet extends DSpaceHttpServlet {

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final
		HttpSession session = request.getSession();

		logger.debug(session.getId() + ": servicing request URI \"" + request.getRequestURI() + "\", query string \"" + request.getQueryString() + "\"");

		Context context = getThreadLocalContext();

		Matcher matcher = createMatcher(request);

		if(!matcher.matches()){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request URI");

			return;
		}

		String handle = matcher.group(1);

		Item item = null;

		try {
			item = ItemUtil.obtainItem(context, handle);
		} catch(SQLException se){
			// Ignored
		}

		if(item == null || item.isWithdrawn()){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Item handle");

			return;
		}

		logger.debug(session.getId() + ": obtained item " + item.getHandle());

		String path = matcher.group(2);

		if(path == null || !(path.startsWith("models/") || path.contains("/models/"))){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad QDB archive path");

			return;
		}

		final
		String query = request.getQueryString();
		if(query == null || "".equals(query)){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad query string");

			return;
		}

		QdbContentCallable<String> callable = new QdbContentCallable<String>(path){

			@Override
			public String call(Object object) throws Exception {

				if(object instanceof Model){
					Model model = (Model)object;

					Map<String, String> parameters = new LinkedHashMap<String, String>();

					String structure = query;

					int ampersand = query.indexOf('&');
					if(ampersand > -1){
						parameters.putAll(parseDescriptors(model.getQdb(), query.substring(ampersand + 1)));

						structure = query.substring(0, ampersand);
					}

					structure = URLDecoder.decode(structure, "US-ASCII");

					logger.debug(session.getId() + ": evaluating \"" + structure + "\" " + (parameters.size() > 0 ? ("(extra parameters " + parameters + ")") : "(no extra parameters)"));

					return PredictorUtil.evaluate(model, parameters, structure);
				}

				throw new IllegalArgumentException();
			}
		};

		String result;

		logger.debug(session.getId() + ": evaluation started");

		try {
			result = QdbUtil.invokeInternal(context, item, callable);
		} catch(Exception e){
			log("Evaluation failed", e);

			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Evaluation failure");

			return;
		} finally {
			logger.debug(session.getId() + ": evaluation finished");
		}

		response.setContentType("text/plain");

		Writer writer = response.getWriter();

		try {
			writer.write(result);
		} finally {
			writer.close();
		}
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

	static
	protected Matcher createMatcher(HttpServletRequest request){
		Pattern pattern = Pattern.compile(request.getContextPath() + request.getServletPath() + "/(\\d+/\\d+)(?:/(.*))?");

		return pattern.matcher(request.getRequestURI());
	}

	private static final Logger logger = Logger.getLogger(PredictorServiceServlet.class);
}