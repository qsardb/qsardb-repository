package org.dspace.qsardb.service.http;

import java.io.*;
import java.util.regex.*;

import javax.servlet.http.*;

import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.qsardb.service.*;

import org.apache.log4j.*;

abstract
public class ItemServlet extends HttpServlet {

	abstract
	protected QdbCallable<? extends Result> createCallable(HttpServletRequest request, HttpServletResponse response, String path) throws IOException;

	public void doService(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Context context = QdbContext.getContext();

		HttpSession session = request.getSession();

		logger.debug(session.getId() + ": servicing request URI \"" + request.getRequestURI() + "\", query string \"" + request.getQueryString() + "\"");

		Matcher matcher = createMatcher(request);

		if(!matcher.matches()){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request URI");

			return;
		}

		String handle = matcher.group(1);
		if(!validateHandle(handle)){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Item handle");

			return;
		}

		String path = matcher.group(2);
		if(!validatePath(path)){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad QDB archive path");

			return;
		}

		Item item = null;

		try {
			item = ItemUtil.obtainItem(context, handle);
		} catch(Exception e){
			// Ignored
		}

		if(item == null || item.isWithdrawn()){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requested Item not found or unavailable");

			return;
		}

		QdbCallable<? extends Result> callable = null;

		try {
			callable = createCallable(request, response, path);
		} catch(Exception e){
			// Ignored
		}

		if(callable == null){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requested operation not valid");

			return;
		}

		logger.debug(session.getId() + ": processing started");

		try {
			Result result = QdbUtil.invokeInternal(context, item, callable);

			result.send(response);
		} catch(Exception e){
			log("Processing failed", e);

			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Service failure");

			return;
		} finally {
			logger.debug(session.getId() + ": processing finished");
		}
	}

	protected boolean validateHandle(String handle){
		return true;
	}

	protected boolean validatePath(String path){
		return true;
	}

	static
	private Matcher createMatcher(HttpServletRequest request){
		Pattern pattern = Pattern.compile(request.getContextPath() + request.getServletPath() + "/(\\d+/\\d+)(?:/(.*))?");

		return pattern.matcher(request.getRequestURI());
	}

	public interface Result {

		void send(HttpServletResponse response) throws IOException;
	}

	static
	public class StringResult implements Result {

		private String string = null;


		public StringResult(String string){
			this.string = string;
		}

		@Override
		public void send(HttpServletResponse response) throws IOException {
			response.setContentType("text/plain; charset=UTF-8");

			Writer writer = response.getWriter();

			try {
				writer.write(this.string);
			} finally {
				writer.close();
			}
		}
	}

	static
	public class ByteArrayResult implements Result {

		private byte[] bytes = null;


		public ByteArrayResult(byte[] bytes){
			this.bytes = bytes;
		}

		@Override
		public void send(HttpServletResponse response) throws IOException {
			response.setContentType("application/octet-stream");
			response.setContentLength(this.bytes.length);

			OutputStream os = response.getOutputStream();

			try {
				os.write(this.bytes);
			} finally {
				os.close();
			}
		}
	}

	private static final Logger logger = Logger.getLogger(ItemServlet.class);
}