package org.dspace.qsardb.client.gwt;

import com.google.gwt.dom.client.*;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.text.shared.*;

import org.dspace.qsardb.rpc.gwt.*;

public class ParameterTooltipCell extends TooltipCell<String> {

	private ParameterTooltip tooltip = null;


	public ParameterTooltipCell(ParameterColumn parameter){
		this.tooltip = new ParameterTooltip(parameter);
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb){

		if(value != null){
			ParameterTooltipCell.renderer.render(value, sb);
		}
	}

	@Override
	public void showTooltip(Context context, Element element, NativeEvent event){
		this.tooltip.schedule(PopupUtil.getPopupX(event) + 5, PopupUtil.getPopupY(event) + 5);
	}

	@Override
	public void hideTooltip(){
		this.tooltip.cancel();
	}

	private static final SafeHtmlRenderer<String> renderer = SimpleSafeHtmlRenderer.getInstance();
}