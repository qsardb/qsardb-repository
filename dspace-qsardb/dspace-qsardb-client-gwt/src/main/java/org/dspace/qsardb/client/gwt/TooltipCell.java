package org.dspace.qsardb.client.gwt;

import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.*;

abstract
public class TooltipCell<C> extends AbstractCell<C> {

	public TooltipCell(){
		super((MouseMoveEvent.getType()).getName(), (MouseOverEvent.getType()).getName(), (MouseOutEvent.getType()).getName());
	}

	abstract
	public void showTooltip(Context context, Element element, NativeEvent event);

	abstract
	public void hideTooltip();


	@Override
	public void onBrowserEvent(Context context, Element element, C value, NativeEvent event, ValueUpdater<C> updater){
		super.onBrowserEvent(context, element, value, event, updater);

		String type = event.getType();

		if((MouseMoveEvent.getType()).getName().equals(type)){
			showTooltip(context, element, event);
		} else

		if((MouseOverEvent.getType()).getName().equals(type) || (MouseOutEvent.getType()).getName().equals(type)){
			hideTooltip();
		}
	}
}