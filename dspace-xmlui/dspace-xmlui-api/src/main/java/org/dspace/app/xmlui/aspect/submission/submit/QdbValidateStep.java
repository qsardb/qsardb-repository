package org.dspace.app.xmlui.aspect.submission.submit;

import java.util.*;

import org.apache.cocoon.environment.*;

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

		Radio levelRadio = (content.addItem()).addRadio("level");
		levelRadio.setLabel(T_level);
		levelRadio.setRequired(true);

		String level = (String)request.get("level");

		Option basic = levelRadio.addOption((Level.BASIC.getValue()).equals(level) || (level == null), Level.BASIC.getValue());
		basic.addContent(T_option_basic);
		basic.addContent(T_option_basic_help, true);

		Option intermediate = levelRadio.addOption((Level.INTERMEDIATE.getValue()).equals(level), Level.INTERMEDIATE.getValue());
		intermediate.addContent(T_option_intermediate);
		intermediate.addContent(T_option_intermediate_help, true);

		Option advanced = levelRadio.addOption((Level.ADVANCED.getValue()).equals(level), Level.ADVANCED.getValue());
		advanced.addContent(T_option_advanced);
		advanced.addContent(T_option_advanced_help, true);

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

			if(level == null){
				division.addPara(T_status_init);
			} else

			{
				division.addPara(T_status_success);
			}
		} else

		if(super.errorFlag == org.dspace.submit.step.QdbValidateStep.STATUS_QDB_ERROR){
			division.addPara(T_status_qdb_error);
		} else

		if(super.errorFlag == org.dspace.submit.step.QdbValidateStep.STATUS_VALIDATION_ERROR){
			division.addPara(T_status_error);
		}

		List controls = division.addList("controls", List.TYPE_FORM);

		addControlButtons(controls);
	}

	@Override
    public List addReviewSection(List reviewList){
    	return null;
    }

	private static final Message T_level = message("xmlui.Submission.submit.QdbValidateStep.level");

	private static final Message T_option_basic = message("xmlui.Submission.submit.QdbValidateStep.option_basic");

	private static final Message T_option_basic_help = message("xmlui.Submission.submit.QdbValidateStep.option_basic_help");

	private static final Message T_option_intermediate = message("xmlui.Submission.submit.QdbValidateStep.option_intermediate");

	private static final Message T_option_intermediate_help = message("xmlui.Submission.submit.QdbValidateStep.option_intermediate_help");

	private static final Message T_option_advanced = message("xmlui.Submission.submit.QdbValidateStep.option_advanced");

	private static final Message T_option_advanced_help = message("xmlui.Submission.submit.QdbValidateStep.option_advanced_help");

	private static final Message T_validate = message("xmlui.Submission.submit.QdbValidateStep.validate");

	private static final Message T_status_init = message("xmlui.Submission.submit.QdbValidateStep.status_init");

	private static final Message T_status_success = message("xmlui.Submission.submit.QdbValidateStep.status_success");

	private static final Message T_status_qdb_error = message("xmlui.Submission.submit.QdbValidateStep.status_qdb_error");

	private static final Message T_status_error = message("xmlui.Submission.submit.QdbValidateStep.status_error");
}