package org.dspace.client.gwt;

import com.google.gwt.event.shared.*;

public class EvaluationEvent extends GwtEvent<EvaluationEventHandler> {

	private String result = null;


	public EvaluationEvent(String result){
		setResult(result);
	}

	@Override
	public void dispatch(EvaluationEventHandler handler){
		handler.onEvaluate(this);
	}

	@Override
	public Type<EvaluationEventHandler> getAssociatedType(){
		return EvaluationEvent.TYPE;
	}

	public String getResult(){
		return this.result;
	}

	private void setResult(String result){
		this.result = result;
	}

	public static final Type<EvaluationEventHandler> TYPE = new Type<EvaluationEventHandler>();
}