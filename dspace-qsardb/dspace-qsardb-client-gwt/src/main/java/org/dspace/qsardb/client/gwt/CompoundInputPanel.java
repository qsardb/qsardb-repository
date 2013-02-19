package org.dspace.qsardb.client.gwt;

import java.util.*;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.qsardb.rpc.gwt.*;

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

		QdbPredictor predictor = (QdbPredictor)Application.getInstance();

		AsyncCallback<Map<String, String>> callback = new ServiceCallback<Map<String, String>>(){

			@Override
			public void onSuccess(Map<String, String> values){
				fireEvent(new InputChangeEvent(values));
			}
		};

		QdbServiceAsync service = (QdbServiceAsync.MANAGER).getInstance();

		try {
			service.calculateModelDescriptors(predictor.getHandle(), predictor.getModelId(), string, callback);
		} catch(DSpaceException de){
			Window.alert("Calculation failed: " + de.getMessage());
		}
	}
}