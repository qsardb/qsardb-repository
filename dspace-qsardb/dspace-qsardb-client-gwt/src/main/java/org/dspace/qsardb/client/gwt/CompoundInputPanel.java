package org.dspace.qsardb.client.gwt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import org.dspace.qsardb.rpc.gwt.DescriptorColumn;
import org.dspace.qsardb.rpc.gwt.QdbTable;

public class CompoundInputPanel extends Composite {
	interface Binder extends UiBinder<Widget, CompoundInputPanel> {}
	private final static Binder binder = GWT.create(Binder.class);

	@UiField TextBox textBox;
	@UiField Button drawButton;
	@UiField Button calculateButton;

	private DrawStructureDialog drawDialog;

	public CompoundInputPanel(QdbTable table){
		Widget panel = binder.createAndBindUi(this);

		boolean calculable = true;
		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for(DescriptorColumn descriptor : descriptors){
			calculable &= descriptor.isCalculable();
		}

		if (!calculable) {
			textBox.setText("Descriptor calculation is not enabled for this model");
			textBox.getElement().setAttribute("style", "color:gray");
		}
		textBox.setEnabled(calculable);

		drawButton.setEnabled(calculable);

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
			fireInputChangeEvent();
		}
	}

	@UiHandler("textBox")
	public void handleTextBoxInput(KeyUpEvent event) {
		calculateButton.setEnabled(!textBox.getValue().isEmpty());
	}

	@UiHandler("textBox")
	public void handleValueChangeEvent(ValueChangeEvent<String> event) {
		calculateButton.setEnabled(!textBox.getValue().isEmpty());
	}

	@UiHandler("drawButton")
	public void handleEditButton(ClickEvent event) {
		if (drawDialog == null) {
			drawDialog = new DrawStructureDialog();
		}
		drawDialog.showEditor(textBox);
	}

	@UiHandler("calculateButton")
	public void handleCalculateButton(ClickEvent event) {
		fireInputChangeEvent();
	}

	private void fireInputChangeEvent() {
		String inputStructure = textBox.getText() != null ? textBox.getText().trim() : "";
		if (inputStructure.isEmpty()) {
			return;
		}

		fireEvent(new InputChangeEvent(inputStructure));
	}
}
