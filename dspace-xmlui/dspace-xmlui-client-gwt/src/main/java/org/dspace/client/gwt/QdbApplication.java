package org.dspace.client.gwt;

import org.dspace.rpc.gwt.*;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

abstract
public class QdbApplication extends Application {

	abstract
	public String getPath();

	abstract
	public Widget createWidget(ModelTable model);

	@Override
	public void onModuleLoad(){
		setWidget(new Label("Loading.."));

		AsyncCallback<ModelTable> callback = new ServiceCallback<ModelTable>(){

			@Override
			public void onSuccess(ModelTable table){
				ParameterUtil.prepareTable(table);

				setWidget(createWidget(table));
			}
		};

		QdbServiceAsync service = (QdbServiceAsync.MANAGER).getInstance();

		try {
			service.loadModelTable(getHandle(), getModelId(), callback);
		} catch(DSpaceException de){
			setWidget(new Label("Loading failed: " + de.getMessage()));
		}
	}

	public String getHandle(){
		return match(getPath() + "/(.*)", 1, Window.Location.getPath());
	}

	public String getModelId(){
		return match("(.*)", 1, Window.Location.getParameter("model"));
	}
}