package org.dspace.qsardb.client.gwt;

import com.google.gwt.core.client.*;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.resources.client.*;
import com.google.gwt.user.client.ui.*;

public class LazyHeader extends LazyPanel implements OpenHandler<DisclosurePanel>, CloseHandler<DisclosurePanel> {

	private Image image = null;


	public LazyHeader(DisclosurePanel panel){
		panel.addOpenHandler(this);
		panel.addCloseHandler(this);
	}

	@Override
	public Widget createWidget(){
		Panel panel = new FlowPanel();

		this.image = new Image(images.expand());

		{
			panel.add(this.image);

			Style style = (this.image.getElement()).getStyle();

			style.setFloat(Style.Float.LEFT);

			style.setMarginTop(1, Style.Unit.PX);
			style.setMarginBottom(1, Style.Unit.PX);

			style.setMarginRight(3, Style.Unit.PX);
		}

		Label left = createLeft();
		if(left != null){
			panel.add(left);

			Style style = (left.getElement()).getStyle();

			style.setFloat(Style.Float.LEFT);
		}

		Label right = createRight();
		if(right != null){
			panel.add(right);

			Style style = (right.getElement()).getStyle();

			style.setFloat(Style.Float.RIGHT);
		}

		return panel;
	}

	public Label createLeft(){
		return null;
	}

	public Label createRight(){
		return null;
	}

	@Override
	public void onOpen(OpenEvent<DisclosurePanel> event){
		this.image.setResource(images.collapse());
	}

	@Override
	public void onClose(CloseEvent<DisclosurePanel> event){
		this.image.setResource(images.expand());
	}

	interface Images extends ClientBundle {

		@Source (
			value = "com/google/gwt/user/client/ui/disclosurePanelClosed.png"
		)
		ImageResource expand();

		@Source (
			value = "com/google/gwt/user/client/ui/disclosurePanelOpen.png"
		)
		ImageResource collapse();
	}

	private static final Images images = GWT.create(Images.class);
}