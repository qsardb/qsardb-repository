package org.dspace.service.http;

import java.io.*;
import java.sql.*;
import java.util.regex.*;

import javax.servlet.http.*;

import org.qsardb.model.*;

import org.dspace.content.*;
import org.dspace.content.QdbUtil;
import org.dspace.core.*;
import org.dspace.service.*;

public class PredictorServiceServlet extends DSpaceHttpServlet {

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		Context context = getThreadLocalContext();

		Matcher matcher = createMatcher(request);

		if(!matcher.matches()){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);

			return;
		}

		Item item = null;

		try {
			item = ItemUtil.obtainItem(context, matcher.group(1));
		} catch(SQLException se){
			// Ignored
		}

		if(item == null || item.isWithdrawn()){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);

			return;
		}

		final
		String string = request.getQueryString();

		if(string == null || ("").equals(string)){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);

			return;
		}

		QdbContentCallable<String> callable = new QdbContentCallable<String>(matcher.group(2)){

			@Override
			public String call(Object object) throws Exception {

				if(object instanceof Model){
					Model model = (Model)object;

					return PredictorUtil.evaluate(model, string);
				}

				throw new IllegalArgumentException();
			}
		};

		String result;

		try {
			result = QdbUtil.invokeInternal(context, item, callable);
		} catch(Exception e){
			log("Evaluation failed", e);

			response.sendError(HttpServletResponse.SC_BAD_REQUEST);

			return;
		}

		response.setContentType("text/plain");

		Writer writer = response.getWriter();

		try {
			writer.write(result);
		} finally {
			writer.close();
		}
	}
}