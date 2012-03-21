package org.dspace.gwt.client;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.gwt.rpc.*;

abstract
public class QdbApplication extends Application {

	abstract
	public String getPath();

	abstract
	public Widget createWidget(ModelTable model);

	@Override
	public void onModuleLoad(){
		setWidget(new Label("Loading.."));

		QdbServiceAsync service = (QdbServiceAsync.MANAGER).getInstance();

		AsyncCallback<ModelTable> callback = new ServiceCallback<ModelTable>(){

			@Override
			public void onSuccess(ModelTable table){
				ParameterUtil.convertTable(table);

				setWidget(createWidget(table));
			}
		};

		try {
			service.loadModelTable(match(getPath() + "/(.*)", 1, Window.Location.getPath()), match("(.*)", 1, Window.Location.getParameter("model")), callback);
		} catch(DSpaceException de){
			setWidget(new Label("Loading failed: " + de.getMessage()));
		}
	}
}