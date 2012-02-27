package org.dspace.gwt.client;

import com.google.gwt.dom.client.*;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import com.reveregroup.gwt.imagepreloader.*;

public class ResolverTooltip extends PopupPanel {

	final
	private Timer timer = new Timer(){

		@Override
		public void run(){
			resolve();
		}
	};

	private ValueCallback callback = null;


	public ResolverTooltip(){
		super(true);
	}

	private void resolve(){
		final
		ValueCallback callback = getCallback();
		if(callback == null){
			return;
		}

		UrlBuilder builder = new UrlBuilder();
		builder.setProtocol("http");
		builder.setHost("cactus.nci.nih.gov");
		builder.setPath("chemical/structure/" + callback.getValue() + "/image");
		builder.setParameter("format", "png");

		ImageLoadHandler loadHandler = new ImageLoadHandler(){

			@Override
			public void imageLoaded(ImageLoadEvent event){

				if(isActive(callback)){
					Dimensions size = event.getDimensions();

					if(size != null){
						setImage(event.getImageUrl(), size.getWidth(), size.getHeight());
					}
				}
			}
		};

		ImagePreloader.load(builder.buildString(), loadHandler);
	}

	private void setImage(String url, int width, int height){
		Image image = new Image(url);
		image.setPixelSize(width, height);
		setWidget(image);

		final
		ValueCallback callback = getCallback();
		if(callback == null){
			return;
		}

		setPopupPositionAndShow(callback);
	}

	public void schedule(String value, final NativeEvent event){
		ValueCallback callback = new ValueCallback(value){

			@Override
			public void setPosition(int width, int height){
				setPopupPosition(event.getScreenX() + 10, event.getScreenY());
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

	private boolean isActive(ValueCallback callback){
		return callback != null && (callback).equals(getCallback());
	}

	private ValueCallback getCallback(){
		return this.callback;
	}

	private void setCallback(ValueCallback callback){
		this.callback = callback;
	}

	static
	abstract
	private class ValueCallback implements PositionCallback {

		private String value = null;


		public ValueCallback(String value){
			setValue(value);
		}

		@Override
		public int hashCode(){
			return getValue().hashCode();
		}

		@Override
		public boolean equals(Object object){

			if(object instanceof ValueCallback){
				ValueCallback that = (ValueCallback)object;

				return (this.getValue()).equals(that.getValue());
			}

			return false;
		}

		public String getValue(){
			return this.value;
		}

		private void setValue(String value){
			this.value = value;
		}
	}

	private static final int TIMER_DELAY = 500;
}