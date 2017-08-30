package org.dspace.qsardb.client.gwt;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DrawStructureDialog {

	interface Binder extends UiBinder<Widget, DrawStructureDialog> {}
	private static final Binder binder = GWT.create(Binder.class);

	@UiField
	DialogBox dialogBox;

	@UiField
	Button cancelButton;

	@UiField
	Button okButton;

	private TextBox target;

	public DrawStructureDialog() {
		Widget widget = binder.createAndBindUi(this);
		widget.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					initJSME();
				}
			}
		});
	}

	private native void initJSME()/*-{
		if(typeof jsmeApplet === 'undefined') {
			jsmeApplet = new $wnd.JSApplet.JSME("jsmeContainer",
				"420px", "320px", {"options" : "oldlook,atommovebutton"}
                    	);
		}
	}-*/;

	private native String getSmiles()/*-{
		return jsmeApplet.smiles();
	}-*/;


	@UiHandler({"cancelButton", "okButton"}) 
	void handleClickEvents(ClickEvent evt) {
		if (evt.getSource().equals(okButton) && target != null) {
			String smiles = getSmiles();
			target.setValue(smiles, true);
		}
		dialogBox.hide();
	}
	
	public void showEditor(TextBox target) {
		this.target = target;
		dialogBox.showRelativeTo(target);
	}
}