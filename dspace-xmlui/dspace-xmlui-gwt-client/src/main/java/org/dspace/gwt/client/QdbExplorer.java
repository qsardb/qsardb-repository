package org.dspace.gwt.client;

import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class QdbExplorer implements EntryPoint {

	@Override
	public void onModuleLoad(){
		RootPanel panel = RootPanel.get("aspect_artifactbrowser_QdbExplorer_div_main");

		if(panel.getWidgetCount() > 0){
			panel.clear();
		}

		panel.add(createContent());
	}

	private Panel createContent(){
		Panel panel = new FlowPanel();

		final
		Label label = new Label("Please wait..");
		panel.add(label);

		AsyncCallback<String> callback = new AsyncCallback<String>(){

			@Override
			public void onSuccess(String string){
				label.setText(string);
			}

			@Override
			public void onFailure(Throwable throwable){
				label.setText(throwable.toString());
			}
		};

		(ExplorerServiceAsync.BROKER.getInstance()).run(callback);

		return panel;
	}

	static {
		GWT.UncaughtExceptionHandler exceptionHandler = new GWT.UncaughtExceptionHandler(){

			@Override
			public void onUncaughtException(Throwable throwable){
				Window.alert(throwable.toString());
			}
		};

		GWT.setUncaughtExceptionHandler(exceptionHandler);
	}
}