package org.dspace.gwt.client;

import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

public class QdbPredictor extends Application {

	@Override
	public String getId(){
		return "aspect_artifactbrowser_QdbPredictor_div_main";
	}

	@Override
	public void onModuleLoad(){
		setWidget(new Label("Loading.."));

		PredictorServiceAsync service = (PredictorServiceAsync.MANAGER).getInstance();

		AsyncCallback<String> callback = new ServiceCallback<String>(){

			@Override
			public void onSuccess(String string){
				setWidget(createWidget(string));
			}
		};

		service.run(callback);
	}

	private Widget createWidget(String string){
		return new Label(string);
	}
}