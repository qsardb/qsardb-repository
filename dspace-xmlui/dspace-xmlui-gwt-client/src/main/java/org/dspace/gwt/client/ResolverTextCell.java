package org.dspace.gwt.client;

import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.text.shared.*;

public class ResolverTextCell extends AbstractSafeHtmlCell<String> {

	private ResolverTooltip tooltip = new ResolverTooltip();


	public ResolverTextCell(){
		this(SimpleSafeHtmlRenderer.getInstance());
	}

	public ResolverTextCell(SafeHtmlRenderer<String> renderer){
		super(renderer, (MouseMoveEvent.getType()).getName(), (MouseOverEvent.getType()).getName(), (MouseOutEvent.getType()).getName());
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> updater){
		super.onBrowserEvent(context, parent, value, event, updater);

		String type = event.getType();

		if((MouseMoveEvent.getType()).getName().equals(type)){
			this.tooltip.schedule(value, event);
		} else

		if((MouseOverEvent.getType()).getName().equals(type) || (MouseOutEvent.getType()).getName().equals(type)){
			this.tooltip.cancel();
		}
	}

	@Override
	public void render(Context context, SafeHtml value, SafeHtmlBuilder sb){

		if(value != null){
			sb.append(value);
		}
	}
}