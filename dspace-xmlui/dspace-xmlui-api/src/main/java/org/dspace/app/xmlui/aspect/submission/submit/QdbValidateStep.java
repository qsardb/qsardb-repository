package org.dspace.app.xmlui.aspect.submission.submit;

import org.apache.cocoon.environment.*;

import org.dspace.app.xmlui.aspect.submission.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.content.*;

public class QdbValidateStep extends AbstractSubmissionStep {

	public QdbValidateStep(){
		super.requireSubmission = true;
	}

	@Override
	public void addBody(Body body) throws WingException {
		Request request = ObjectModelHelper.getRequest(getObjectModel());

		Collection collection = super.submission.getCollection();

		String actionUrl = super.contextPath + "/handle/" + collection.getHandle() + "/submit/" + super.knot.getId() + ".continue";

		Division division = body.addInteractiveDivision("submit-validate", actionUrl, Division.METHOD_POST, "primary submission");
		division.setHead(T_submission_head);

		addSubmissionProgressList(division);

		List content = division.addList("validate", List.TYPE_FORM);

		Select levelSelect = (content.addItem()).addSelect("level");
		levelSelect.setLabel(T_level);
		levelSelect.setHelp(T_level_help);
		levelSelect.setRequired(true);

		String level = (String)request.get("level");

		levelSelect.addOption("basic".equals(level), "basic", T_option_basic);
		levelSelect.addOption("intermediate".equals(level), "intermediate", T_option_intermediate);
		levelSelect.addOption("advanced".equals(level), "advanced", T_option_advanced);

		Button validateButton = (content.addItem()).addButton("validate");
		validateButton.setValue(T_validate);

		if(super.errorFlag == org.dspace.submit.step.QdbValidateStep.STATUS_QDB_ERROR){
			division.addPara(T_qdb_error);
		} else

		if(super.errorFlag == org.dspace.submit.step.QdbValidateStep.STATUS_VALIDATION_ERROR){
			division.addPara(T_validation_error);
		} // End if

		// Give feedback for selected conformance level
		if(super.errorFlag == org.dspace.submit.step.QdbValidateStep.STATUS_COMPLETE && level != null){
			division.addPara(T_validation_success);
		}

		List controls = division.addList("controls", List.TYPE_FORM);

		addControlButtons(controls);
	}

	@Override
    public List addReviewSection(List reviewList){
    	return null;
    }

	private static final Message T_level = message("xmlui.Submission.submit.QdbValidateStep.level");

	private static final Message T_level_help = message("xmlui.Submission.submit.QdbValidateStep.level_help");

	private static final Message T_option_basic = message("xmlui.Submission.submit.QdbValidateStep.option_basic");

	private static final Message T_option_intermediate = message("xmlui.Submission.submit.QdbValidateStep.option_intermediate");

	private static final Message T_option_advanced = message("xmlui.Submission.submit.QdbValidateStep.option_advanced");

	private static final Message T_validate = message("xmlui.Submission.submit.QdbValidateStep.validate");

	private static final Message T_qdb_error = message("xmlui.Submission.submit.QdbValidateStep.qdb_error");

	private static final Message T_validation_error = message("xmlui.Submission.submit.QdbValidateStep.validation_error");

	private static final Message T_validation_success = message("xmlui.Submission.submit.QdbValidateStep.validation_success");
}