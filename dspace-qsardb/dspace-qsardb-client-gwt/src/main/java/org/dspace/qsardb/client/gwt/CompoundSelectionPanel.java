package org.dspace.qsardb.client.gwt;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import java.util.ArrayList;
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
	
	@UiField
	Button predictButton;

	CompoundSelectionPanel(QdbTable table) {
		this.table = table;
		this.names = table.getColumn(NameColumn.class).getValues();
		initWidget(createUI(table));
	}

	private Widget createUI(QdbTable table) {
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
	
	@UiHandler("predictButton")
	void handlePredict(ClickEvent evt) {
		String selection = suggestBox.getText();

		String input = names.containsKey(selection) ? selection : "";

		if (input.isEmpty()) {
			for (Map.Entry<String, String> e: names.entrySet()) {
				if (e.getValue().equalsIgnoreCase(selection)) {
					input = e.getKey();
					break;
				}
			}
		}

		if (input.isEmpty()) {
			Window.alert("Compound not found: "+selection);
		} else {
			updateSelection(input);
		}
	}

	@Override
	public void onEvent(CompoundBrowseEvent e) {
		suggestBox.setText(e.compoundId); // XXX
		updateSelection(e.compoundId);
	}

	public HandlerRegistration addInputChangeEventHandler(InputChangeEventHandler handler){
		return addHandler(handler, InputChangeEvent.TYPE);
	}

}
