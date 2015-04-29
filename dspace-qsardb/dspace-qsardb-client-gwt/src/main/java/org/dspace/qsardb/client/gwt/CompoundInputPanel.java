package org.dspace.qsardb.client.gwt;

import java.util.*;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.qsardb.rpc.gwt.*;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

public class CompoundInputPanel extends Composite {

	public CompoundInputPanel(QdbTable table){
		Panel panel = new VerticalPanel();

		boolean calculable = true;

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for(DescriptorColumn descriptor : descriptors){
			calculable &= descriptor.isCalculable();
		}

		panel.add(new HTML("<u>Chemical structure input</u> (InChI or SMILES format):"));

		final
		TextBox textBox = new TextBox();
		textBox.setVisibleLength(60);
		textBox.setEnabled(calculable);

		panel.add(textBox);

		final
		KeyPressHandler keyHandler = new KeyPressHandler(){

			@Override
			public void onKeyPress(KeyPressEvent event){
				int charCode = event.getUnicodeCharCode();

				// Not an alphanumeric key
				if(charCode == 0){
					int keyCode = (event.getNativeEvent()).getKeyCode();

					if(keyCode == KeyCodes.KEY_ENTER){
						onEnter();
					}
				} else

				if(charCode == KeyCodes.KEY_ENTER){
					onEnter();
				}
			}

			private void onEnter(){
				calculate(textBox.getText());
			}
		};
		textBox.addKeyPressHandler(keyHandler);

		final
		Button button = new Button("Calculate");
		button.setEnabled(calculable);

		panel.add(button);

		ClickHandler clickHandler = new ClickHandler(){

			@Override
			public void onClick(ClickEvent event){
				calculate(textBox.getText());
			}
		};
		button.addClickHandler(clickHandler);

		initWidget(panel);
	}

	public HandlerRegistration addInputChangeEventHandler(InputChangeEventHandler handler){
		return addHandler(handler, InputChangeEvent.TYPE);
	}

	private void calculate(String string){

		if(string != null){
			string = string.trim();

			if("".equals(string)){
				return;
			}
		} else

		{
			return;
		}

		PredictorRequest request = new PredictorRequest(string);
		PredictorClient.predict(request, new MethodCallback<PredictorResponse>() {
			@Override
			public void onFailure(Method method, Throwable ex) {
				Window.alert("Calculation failed: " + ex.getMessage());
			}

			@Override
			public void onSuccess(Method method, PredictorResponse response) {
				fireEvent(new InputChangeEvent(response.getParameters()));
			}
		});
	}
}