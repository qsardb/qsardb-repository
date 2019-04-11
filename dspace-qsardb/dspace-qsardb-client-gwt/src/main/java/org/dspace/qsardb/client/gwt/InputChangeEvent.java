package org.dspace.qsardb.client.gwt;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import java.util.Map;

public class InputChangeEvent extends GwtEvent<InputChangeEventHandler> {

	private Map<String, String> values = null;
	private String structure = null;
	private String compoundId = null;

	public static final Type<InputChangeEventHandler> TYPE = new Type<InputChangeEventHandler>();

	public InputChangeEvent(Map<String, String> values) {
		this.values = values;
	}

	public InputChangeEvent(String compoundId, Map<String, String> values) {
		this.values = values;
		this.compoundId = compoundId;
	}

	public InputChangeEvent(String structure) {
		this.structure = structure;
	}

	public Map<String, String> getValues() {
		return this.values;
	}

	public String getCompoundId() {
		return this.compoundId;
	}

	public String getStructure() {
		return this.structure;
	}

	@Override
	public void dispatch(InputChangeEventHandler handler) {
		handler.onInputChanged(this);
	}

	@Override
	public Type<InputChangeEventHandler> getAssociatedType() {
		return InputChangeEvent.TYPE;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(InputChangeEvent.class.getSimpleName());
		if (getSource() != null) {
			sb.append(":source=").append(getSource().getClass().getSimpleName());
		}
		sb.append(":structure=").append(structure);
		sb.append(":values=").append(values);
		return sb.toString();
	}
}
