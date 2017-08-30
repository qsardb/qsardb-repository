package org.dspace.app.xmlui.aspect.submission.submit;

import javax.servlet.*;

import org.dspace.app.util.*;
import org.dspace.content.*;

public class QdbDescribeStep extends DescribeStep {

	public QdbDescribeStep() throws ServletException {
		super();
	}

	@Override
	public DCInput[] filterInputs(DCInput[] inputs){
		Item item = super.submissionInfo.getSubmissionItem().getItem();

		return org.dspace.submit.step.QdbDescribeStep.filterBibTeXInput(item, inputs);
	}
}