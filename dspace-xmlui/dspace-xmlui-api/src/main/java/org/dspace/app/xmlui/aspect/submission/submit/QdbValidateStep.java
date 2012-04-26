package org.dspace.app.xmlui.aspect.submission.submit;

import java.util.*;

import org.apache.cocoon.environment.*;
import org.apache.log4j.*;

import org.dspace.app.xmlui.aspect.submission.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.submit.step.*;
import org.dspace.submit.step.QdbValidateStep.Level;

public class QdbValidateStep extends AbstractSubmissionStep {

	public QdbValidateStep(){
		super.requireSubmission = true;
	}

	@Override
	public void addBody(Body body) throws WingException {
		Item item = super.submissionInfo.getSubmissionItem().getItem();

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

		levelSelect.addOption((Level.BASIC.getValue()).equals(level), Level.BASIC.getValue(), T_option_basic);
		levelSelect.addOption((Level.INTERMEDIATE.getValue()).equals(level), Level.INTERMEDIATE.getValue(), T_option_intermediate);
		levelSelect.addOption((Level.ADVANCED.getValue()).equals(level), Level.ADVANCED.getValue(), T_option_advanced);

		Button validateButton = (content.addItem()).addButton("validate");
		validateButton.setValue(T_validate);

		java.util.List<org.qsardb.validation.Message> messages = Collections.<org.qsardb.validation.Message>emptyList();

		try {
			ItemMessageCollector collector = ItemMessageCollector.load(item);

			if(collector != null){
				messages = collector.getMessages();
			}
		} catch(Exception e){
			// Ignored
		}

		if(messages.size() > 0){
			Table messagesTable = division.addTable("messages", messages.size(), 2);

			if(true){
				Row headerRow = messagesTable.addRow("header");

				Cell pathCell = headerRow.addCell("header");
				pathCell.addContent("Archive path");

				Cell contentCell = headerRow.addCell("header");
				contentCell.addContent("Content");
			}

			for(org.qsardb.validation.Message message : messages){
				Row messageRow = messagesTable.addRow(null, "data", "message-" + (message.getLevel()).getValue());

				messageRow.addCellContent(message.getPath());
				messageRow.addCellContent(message.getContent());
			}
		} // End if

		if(super.errorFlag == org.dspace.submit.step.QdbValidateStep.STATUS_COMPLETE){

			// Check that the user has really submitted the validation form
			if(level != null){
				division.addPara(T_validation_success);
			}
		} else

		if(super.errorFlag == org.dspace.submit.step.QdbValidateStep.STATUS_QDB_ERROR){
			division.addPara(T_qdb_error);
		} else

		if(super.errorFlag == org.dspace.submit.step.QdbValidateStep.STATUS_VALIDATION_ERROR){
			division.addPara(T_validation_error);
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