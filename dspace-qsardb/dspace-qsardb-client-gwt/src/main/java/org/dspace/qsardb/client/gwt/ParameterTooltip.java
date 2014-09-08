package org.dspace.qsardb.client.gwt;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import org.dspace.qsardb.rpc.gwt.*;

public class ParameterTooltip extends Tooltip {

	private ParameterColumn parameter = null;

	final
	private Timer timer = new Timer(){

		@Override
		public void run(){
			show();
		}
	};


	public ParameterTooltip(ParameterColumn parameter){
		setParameter(parameter);

		setWidget(new Label(parameter.getName()));
	}

	public void schedule(final int left, final int top){
		setPopupPosition(left, top);

		this.timer.schedule(SHOW_DELAY);
	}

	public void cancel(){
		this.timer.cancel();

		if(isShowing()){
			hide();
		}
	}

	public ParameterColumn getParameter(){
		return this.parameter;
	}

	private void setParameter(ParameterColumn parameter){
		this.parameter = parameter;
	}
}