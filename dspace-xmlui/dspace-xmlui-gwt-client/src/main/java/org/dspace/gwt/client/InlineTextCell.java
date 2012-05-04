package org.dspace.gwt.client;

import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.text.shared.*;

public class InlineTextCell extends AbstractCell<String> {

	private ResolverTooltip tooltip = null;


	public InlineTextCell(){
	}

	public InlineTextCell(Resolver resolver){
		super((MouseMoveEvent.getType()).getName(), (MouseOverEvent.getType()).getName(), (MouseOutEvent.getType()).getName());

		this.tooltip = new ResolverTooltip(resolver);
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb){

		if(value != null){
			sb.append(START_TAG);

			InlineTextCell.renderer.render(value, sb);

			sb.append(END_TAG);
		}
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> updater){
		super.onBrowserEvent(context, parent, value, event, updater);

		if(this.tooltip == null){
			return;
		}

		String type = event.getType();

		if((MouseMoveEvent.getType()).getName().equals(type)){
			Compound compound = (Compound)context.getKey();

			this.tooltip.schedule(compound.getId(), PopupUtil.getPopupX(event) + 5, PopupUtil.getPopupY(event) + 5);
		} else

		if((MouseOverEvent.getType()).getName().equals(type) || (MouseOutEvent.getType()).getName().equals(type)){
			this.tooltip.cancel();
		}
	}

	private static final SafeHtml START_TAG = new SafeHtmlString("<span class=\"inlineTextCell\">");

	private static final SafeHtml END_TAG = new SafeHtmlString("</span>");

	private static final SafeHtmlRenderer<String> renderer = SimpleSafeHtmlRenderer.getInstance();
}