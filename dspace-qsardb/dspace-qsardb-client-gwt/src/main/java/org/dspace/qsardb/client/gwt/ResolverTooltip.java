package org.dspace.qsardb.client.gwt;

import com.google.gwt.dom.client.Style;
import java.util.*;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;

import com.reveregroup.gwt.imagepreloader.*;

public class ResolverTooltip extends Tooltip {

	private final Resolver resolver;
	private String scheduledKey;

	private FlexTable table = null;

	public ResolverTooltip(Resolver resolver){
		this.resolver = resolver;
		this.table = new FlexTable();
		this.table.addStyleName("resolver-tooltip");
		setWidget(this.table);
	}

	@Override
	protected void render() {
		final String resolveKey = scheduledKey;
		if(resolveKey == null){
			return;
		}

		final Map<String, String> values = resolver.resolve(resolveKey);
		final String resolveMethod = resolver.resolveMethod(values);

		resetTable(null, values, resolveMethod);
		setPopupPositionAndShow(getPositionCallback());

		ImageLoadHandler loadHandler = new ImageLoadHandler(){

			@Override
			public void imageLoaded(ImageLoadEvent event){
				if(resolveKey.equals(scheduledKey)){
					Image image = null;

					Dimensions size = event.getDimensions();
					if(size != null){
						image = new Image(event.getImageUrl());
						image.setPixelSize(size.getWidth(), size.getHeight());
					}

					resetImage(image, resolveMethod);
				}
			}
		};

		String url = resolver.resolveURL(values);
		ImagePreloader.load(url, loadHandler);

	}

	private void resetTable(Image image, Map<String, String> values, String method){

		if(this.table.getRowCount() > 0){
			this.table.removeAllRows();
		}

		FlexTable.FlexCellFormatter formatter = (FlexTable.FlexCellFormatter)this.table.getCellFormatter();

		int row = 0;

		formatter.setColSpan(row, 0, 2);

		resetImage(image, "Resolving "+method+"...");
		formatter.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);

		row++;

		Set<Map.Entry<String, String>> entries = values.entrySet();
		for(Map.Entry<String, String> entry : entries){
			String key = entry.getKey();
			String value = entry.getValue();

			if(value == null || value.isEmpty()){
				continue;
			}

			this.table.setWidget(row, 0, new HTML("<b>" + key + "</b>"));
			formatter.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);

			this.table.setWidget(row, 1, new HTML(value));
			formatter.setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);

			row++;
		}
	}

	private void resetImage(Image image, String method){
		FlowPanel w = new FlowPanel();
		w.setStylePrimaryName("depict");
		if (image != null) {
			w.add(image);
			w.add(new InlineLabel("[ "+method+" ]"));
		}

		this.table.setWidget(0, 0, w);

		if (image != null) {
			int top = getPopupTop();
			int height = getOffsetHeight();
			if ((top + height) > (Window.getScrollTop() + Window.getClientHeight())) {
				top = Window.getScrollTop() + Window.getClientHeight() - height;
			}
			setPopupPosition(getPopupLeft(), top);
		}
	}

	public void schedule(String id, final int x, final int y){
		scheduledKey = id;
		super.schedule(x, y);
	}

	@Override
	public void cancel(){
		super.cancel();
	}
}