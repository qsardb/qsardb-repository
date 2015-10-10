package org.dspace.qsardb.client.gwt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.LinkedHashMap;
import java.util.Map;
import org.dspace.qsardb.rpc.gwt.DescriptorColumn;
import org.dspace.qsardb.rpc.gwt.NameColumn;
import org.dspace.qsardb.rpc.gwt.QdbTable;

class CompoundSelectionPanel extends Composite implements CompoundBrowseEvent.Handler {

	private final QdbTable table;
	private final Map<String, String> names;
	private CompoundBrowseDialog dialog;

	interface Binder extends UiBinder<Widget, CompoundSelectionPanel> {}
	private final static Binder binder = GWT.create(Binder.class);
	
	@UiField
	Button browseButton;

	@UiField(provided = true)
	SuggestBox suggestBox;
	
	CompoundSelectionPanel(QdbTable table) {
		this.table = table;
		this.names = table.getColumn(NameColumn.class).getValues();
		initWidget(createUI());
	}

	private Widget createUI() {
		MultiWordSuggestOracle compoundOracle = new MultiWordSuggestOracle(" ,.-()[]");
		compoundOracle.addAll(names.values());
		compoundOracle.addAll(names.keySet());
                
		suggestBox = new SuggestBox(compoundOracle);
		suggestBox.getElement().setAttribute("placeholder", "Compound name or ID.");

		return binder.createAndBindUi(this);
	}

	public void updateSelection(String compoundId) {
		Map<String, String> descValues = new LinkedHashMap<String, String>();
		for (DescriptorColumn d: table.getAllColumns(DescriptorColumn.class)) {
			descValues.put(d.getId(), d.getValue(compoundId).toString());
		}

		QdbPredictor predictor = (QdbPredictor)Application.getInstance();
		if (predictor.getDataInputPanel().compoundInputPanel.textBox.isEnabled()) {
			predictor.getDataInputPanel().compoundInputPanel.textBox.setValue("", false);
		}

		fireEvent(new InputChangeEvent(descValues));
	}

	@UiHandler("browseButton")
	void handleBrowse(ClickEvent e) {
		if (dialog == null) {
			dialog = new CompoundBrowseDialog(table);
			dialog.addEventHandler(this);
		}
		dialog.showRelativeTo(browseButton);
	}
	
	@Override
	public void onEvent(CompoundBrowseEvent e) {
		String name = table.getColumn(NameColumn.class).getValue(e.compoundId);
		suggestBox.setText(name);
		updateSelection(e.compoundId);
	}

	public HandlerRegistration addInputChangeEventHandler(InputChangeEventHandler handler){
		return addHandler(handler, InputChangeEvent.TYPE);
	}

}
