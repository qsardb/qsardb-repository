package org.dspace.gwt.client;

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

	public void setWidget(Widget widget){
		RootPanel panel = RootPanel.get(getId());

		if(panel.getWidgetCount() > 0){
			panel.clear();
		}

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

				String message = throwable.toString();

				Throwable causeThrowable = throwable.getCause();
				if(causeThrowable != null){
					message += " (" + causeThrowable.toString() + ")";
				}

				Window.alert(message);
			}
		};

		GWT.setUncaughtExceptionHandler(exceptionHandler);
	}
}