package org.dspace.qsardb.client.gwt;

import com.google.gwt.core.client.*;
import com.google.gwt.regexp.shared.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

abstract
public class Application implements EntryPoint {

	Application(){
		setInstance(this);
	}

	abstract
	public String getId();

	abstract
	public String getPath();

	public String getContextPath() {
		return match("(/.*)/"+getPath(), 1, Window.Location.getPath());
	}

	public void setWidget(Widget widget){
		RootPanel panel = RootPanel.get(getId());

		if(panel.getWidgetCount() > 0){
			panel.clear();
		}

		Element element = panel.getElement();
		element.setInnerHTML("");

		panel.add(widget);
	}

	static
	public String match(String pattern, int group, String string){
		RegExp regexp = RegExp.compile(pattern);

		if(validate(string)){
			MatchResult match = regexp.exec(string);

			if(match != null){
				String result = match.getGroup(group);

				if(validate(result)){
					return result;
				}
			}
		}

		throw new IllegalArgumentException("RegExp pattern \'" + pattern + "\' does not match \'" + string + "\'");
	}

	static
	private boolean validate(String string){
		return (string != null && string.length() > 0);
	}

	static
	public Application getInstance(){
		return Application.instance;
	}

	static
	private void setInstance(Application instance){
		Application.instance = instance;
	}

	private static Application instance = null;

	static {
		GWT.UncaughtExceptionHandler exceptionHandler = new GWT.UncaughtExceptionHandler(){

			@Override
			public void onUncaughtException(Throwable throwable){
				throwable.printStackTrace();

				StringBuffer sb = new StringBuffer();

				sb.append(format(throwable));

				Throwable cause = throwable.getCause();
				if(cause != null){

					while(true){
						Throwable nextCause = cause.getCause();
						if(nextCause == null){
							break;
						}

						cause = nextCause;
					}

					sb.append("\n");
					sb.append("Caused by:");

					sb.append("\n");
					sb.append(format(cause));
				}

				Window.alert(sb.toString());
			}

			private String format(Throwable throwable){
				StringBuffer sb = new StringBuffer();

				sb.append(throwable.toString());

				StackTraceElement[] elements = throwable.getStackTrace();
				for(StackTraceElement element : elements){
					sb.append("\n");
					sb.append(element.toString());
				}

				return sb.toString();
			}
		};

		GWT.setUncaughtExceptionHandler(exceptionHandler);
	}
}