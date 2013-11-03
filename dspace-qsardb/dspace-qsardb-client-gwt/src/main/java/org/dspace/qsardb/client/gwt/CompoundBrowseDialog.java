package org.dspace.qsardb.client.gwt;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import java.util.ArrayList;
import org.dspace.qsardb.rpc.gwt.IdColumn;
import org.dspace.qsardb.rpc.gwt.NameColumn;
import org.dspace.qsardb.rpc.gwt.QdbTable;

public class CompoundBrowseDialog {
	private final QdbTable table;
	private final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();

	interface Binder extends UiBinder<Widget, CompoundBrowseDialog> {}
	private static final Binder binder = GWT.create(Binder.class);

	@UiField
	DialogBox dialogBox;

	@UiField(provided = true)
	CellList<String> cellList;

	@UiField
	Button cancelButton;

	@UiField
	Button okButton;

	public CompoundBrowseDialog(QdbTable table) {
		this.table = table;
		initUI();
	}

	private void initUI() {
		cellList = new CellList<String>(new CompoundCell());
		cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
		cellList.setSelectionModel(selectionModel);
		binder.createAndBindUi(this);

		ArrayList<String> compIds = new ArrayList<String>(table.getKeys());
		cellList.setRowCount(compIds.size(), true);
		cellList.setRowData(compIds);
	}

	@UiHandler({"cancelButton", "okButton"}) 
	public void handleButtonClicks(ClickEvent evt) {
		dialogBox.hide();
		if (evt.getSource().equals(okButton)) {
			String selCompId = selectionModel.getSelectedObject();
			fireEvent(selCompId);
		}
	}

	private class CompoundCell extends AbstractCell<String> {
		@Override
		public void render(Context context, String value, SafeHtmlBuilder sb) {
			if (value == null) {
				return;
			}
			String id = table.getColumn(IdColumn.class).getValue(value);
			String name = table.getColumn(NameColumn.class).getValue(value);
			sb.appendEscaped(id +": "+name);
		}
	}
	
	public HandlerRegistration addEventHandler(CompoundBrowseEvent.Handler handler) {
		return dialogBox.addHandler(handler, CompoundBrowseEvent.TYPE);
	}

	private void fireEvent(String compoundId) {
		dialogBox.fireEvent(new CompoundBrowseEvent(compoundId));
	}

	void showRelativeTo(Button target) {
		dialogBox.showRelativeTo(target);
	}

}