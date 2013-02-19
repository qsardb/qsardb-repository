package org.dspace.submit.step;

import org.dspace.app.util.*;
import org.dspace.content.*;
import org.dspace.submit.*;

public class SubmissionUtil {

	private SubmissionUtil(){
	}

	static
	public boolean isComplete(AbstractProcessingStep step, SubmissionInfo submissionInfo){
		String stepClass = (step.getClass()).getName();

		InProgressSubmission submission = submissionInfo.getSubmissionItem();

		if(submission instanceof WorkspaceItem){
			WorkspaceItem workspaceItem = (WorkspaceItem)submission;

			if(getStage(stepClass, submissionInfo.getSubmissionConfig()) < workspaceItem.getStageReached()){
				return true;
			}
		}

		return false;
	}

	static
	private int getStage(String stepClass, SubmissionConfig submissionConfig){
		int steps = submissionConfig.getNumberOfSteps();

		for(int i = 0; i < steps; i++){
			SubmissionStepConfig stepConfig = submissionConfig.getStep(i);

			if((stepConfig.getProcessingClassName()).equals(stepClass)){
				return stepConfig.getStepNumber();
			}
		}

		throw new IllegalArgumentException(stepClass);
	}
}