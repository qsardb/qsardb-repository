package org.dspace.qsardb.client.gwt;

import com.google.gwt.event.shared.*;
import org.dspace.qsardb.rpc.gwt.PredictorResponse;

public class EvaluationEvent extends GwtEvent<EvaluationEventHandler> {

	private String result = null;
	private PredictorResponse response = null;
	public static final Type<EvaluationEventHandler> TYPE = new Type<EvaluationEventHandler>();



	public EvaluationEvent(String result){
		setResult(result);
	}

	public EvaluationEvent(PredictorResponse response){
		this.response = response;
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

	public PredictorResponse getResponse() {
		return response;
	}

	public void setResponse(PredictorResponse response) {
		this.response = response;
	}

}
