package org.dspace.qsardb.client.gwt;

import com.google.gwt.dom.client.*;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.text.shared.*;

public class ResolverTooltipCell extends TooltipCell<String> {

	private ResolverTooltip tooltip = null;


	public ResolverTooltipCell(Resolver resolver){
		this.tooltip = new ResolverTooltip(resolver);
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb){

		if(value != null){
			ResolverTooltipCell.renderer.render(value, sb);
		}
	}

	@Override
	public void showTooltip(Context context, Element element, NativeEvent event){
		Compound compound = (Compound)context.getKey();

		this.tooltip.schedule(compound.getId(), PopupUtil.getPopupX(event), PopupUtil.getPopupY(event));
	}

	@Override
	public void hideTooltip(){
		this.tooltip.cancel();
	}

	private static final SafeHtmlRenderer<String> renderer = SimpleSafeHtmlRenderer.getInstance();
}