package org.dspace.qsardb.client.gwt;

import java.util.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.qsardb.rpc.gwt.*;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

public class CompoundInputPanel extends Composite {
	interface Binder extends UiBinder<Widget, CompoundInputPanel> {}
	private final static Binder binder = GWT.create(Binder.class);
    
	@UiField TextBox textBox;
	@UiField Button calculateButton;

	public CompoundInputPanel(QdbTable table){
		Widget panel = binder.createAndBindUi(this);

		boolean calculable = true;
		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for(DescriptorColumn descriptor : descriptors){
			calculable &= descriptor.isCalculable();
		}

		if (!calculable) {
			textBox.setText("Descriptors in this model require proprietary software and can not be calculated");
			textBox.getElement().setAttribute("style", "color:gray");
		}
		textBox.setEnabled(calculable);

		calculateButton.setEnabled(false);

		initWidget(panel);
	}

	public HandlerRegistration addInputChangeEventHandler(InputChangeEventHandler handler){
		return addHandler(handler, InputChangeEvent.TYPE);
	}

	@UiHandler("textBox")
	public void handleReturnKey(KeyPressEvent event) {
		int keyCode = event.getUnicodeCharCode();
		if (keyCode == 0) {
			keyCode = event.getNativeEvent().getKeyCode();
		}
		if (keyCode == KeyCodes.KEY_ENTER) {
			calculate();
		}
	}

	@UiHandler("textBox")
	public void handleTextBoxInput(KeyUpEvent event) {
		calculateButton.setEnabled(!textBox.getValue().isEmpty());
	}

	@UiHandler("calculateButton")
	public void handleClick(ClickEvent event) {
		calculate();
	}

	public void calculate(){
		String inputStructure = textBox.getText() != null ? textBox.getText().trim() : "";
		if ("".equals(inputStructure)) {
			return;
		}

		PredictorRequest request = new PredictorRequest(inputStructure);
		PredictorClient.predict(request, new MethodCallback<PredictorResponse>() {
			@Override
			public void onFailure(Method method, Throwable ex) {
				Window.alert("Calculation failed: " + ex.getMessage());
			}

			@Override
			public void onSuccess(Method method, PredictorResponse response) {
				fireEvent(new InputChangeEvent(response));
			}
		});
	}
}
