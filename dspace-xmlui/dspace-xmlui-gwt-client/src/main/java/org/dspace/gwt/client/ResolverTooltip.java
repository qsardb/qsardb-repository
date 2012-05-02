package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;

import com.reveregroup.gwt.imagepreloader.*;

public class ResolverTooltip extends PopupPanel {

	private Resolver resolver = null;

	private FlexTable table = null;

	final
	private Timer timer = new Timer(){

		@Override
		public void run(){
			resolve();
		}
	};

	private IdCallback callback = null;


	public ResolverTooltip(Resolver resolver){
		super(true);

		setResolver(resolver);

		this.table = new FlexTable();
		setWidget(this.table);
	}

	private void resolve(){
		final
		IdCallback callback = getCallback();
		if(callback == null){
			return;
		}

		Resolver resolver = getResolver();

		final
		Map<String, String> values = resolver.resolve(callback.getId());

		final
		String imageValue = getValue(values, Arrays.asList(Resolver.INCHI, Resolver.SMILES, Resolver.NAME));

		StringBuilder sb = new StringBuilder();
		sb.append("http://cactus.nci.nih.gov");
		sb.append("/chemical/structure/").append(imageValue.replace("#", "%23").replace("?", "%3f")).append("/image").append("?").append("format=png");

		String url = sb.toString();

		ImageLoadHandler loadHandler = new ImageLoadHandler(){

			@Override
			public void imageLoaded(ImageLoadEvent event){

				if(isActive(callback)){
					Image image = null;

					Dimensions size = event.getDimensions();
					if(size != null){
						image = new Image(event.getImageUrl());
						image.setPixelSize(size.getWidth(), size.getHeight());
					}

					resetTable(image, values, image != null ? imageValue : null);

					setPopupPositionAndShow(callback);
				}
			}
		};

		ImagePreloader.load(url, loadHandler);
	}

	private void resetTable(Image image, Map<String, String> values, String imageValue){

		if(this.table.getRowCount() > 0){
			this.table.removeAllRows();
		}

		FlexTable.FlexCellFormatter formatter = (FlexTable.FlexCellFormatter)this.table.getCellFormatter();

		int row = 0;

		if(image != null){
			formatter.setColSpan(row, 0, 2);

			this.table.setWidget(row, 0, image);
			formatter.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);

			row++;
		}

		Set<Map.Entry<String, String>> entries = values.entrySet();
		for(Map.Entry<String, String> entry : entries){
			String key = entry.getKey();
			String value = entry.getValue();

			if(value == null){
				continue;
			}

			this.table.setWidget(row, 0, new HTML("<b>" + key + "</b>")); // XXX
			formatter.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);

			this.table.setWidget(row, 1, new HTML((value).equals(imageValue) ? "<u>" + value + "</u>" : value)); // XXX
			formatter.setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);

			row++;
		}
	}

	public void schedule(String id, final int x, final int y){
		IdCallback callback = new IdCallback(id){

			@Override
			public void setPosition(int width, int height){
				setPopupPosition(x, y);
			}
		};

		// Update position, but do not restart timer
		if(isActive(callback)){
			setCallback(callback);

			return;
		}

		setCallback(callback);

		this.timer.schedule(TIMER_DELAY);
	}

	public void cancel(){
		setCallback(null);

		this.timer.cancel();

		if(isShowing()){
			hide();
		}
	}

	private boolean isActive(IdCallback callback){
		return callback != null && (callback).equals(getCallback());
	}

	public Resolver getResolver(){
		return this.resolver;
	}

	private void setResolver(Resolver resolver){
		this.resolver = resolver;
	}

	private IdCallback getCallback(){
		return this.callback;
	}

	private void setCallback(IdCallback callback){
		this.callback = callback;
	}

	static
	public String getValue(Map<String, String> values, List<String> keys){

		for(String key : keys){
			String value = values.get(key);

			if(value != null){
				return value;
			}
		}

		return null;
	}

	static
	abstract
	private class IdCallback implements PositionCallback {

		private String id = null;


		public IdCallback(String id){
			setId(id);
		}

		@Override
		public int hashCode(){
			return getId().hashCode();
		}

		@Override
		public boolean equals(Object object){

			if(object instanceof IdCallback){
				IdCallback that = (IdCallback)object;

				return (this.getId()).equals(that.getId());
			}

			return false;
		}

		public String getId(){
			return this.id;
		}

		private void setId(String id){
			this.id = id;
		}
	}

	private static final int TIMER_DELAY = 500;
}