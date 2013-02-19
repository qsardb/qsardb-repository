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

		java.util.List<org.qsardb.validation.Message> messages = new ArrayList<org.qsardb.validation.Message>();

		try {
			ItemMessageCollector collector = ItemMessageCollector.load(item);

			if(collector != null){

				if(level == null){
					level = collector.getLevel();

					super.errorFlag = org.dspace.submit.step.QdbValidateStep.getStatus(collector);
				}

				messages.addAll(collector.getMessages());
			}
		} catch(Exception e){
			// Ignored
		}

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

		if(messages.size() > 0){
			Table messagesTable = division.addTable("messages", messages.size(), 3);

			if(true){
				Row headerRow = messagesTable.addRow(Row.ROLE_HEADER);

				Cell imageCell = headerRow.addCell(null, Cell.ROLE_HEADER, "icon");
				imageCell.addContent("");

				Cell pathCell = headerRow.addCell(null, Cell.ROLE_HEADER, "long");
				pathCell.addContent("QDB archive path");

				Cell contentCell = headerRow.addCell(null, Cell.ROLE_HEADER, null);
				contentCell.addContent("Message");
			}

			Comparator<org.qsardb.validation.Message> comparator = new Comparator<org.qsardb.validation.Message>(){

				@Override
				public int compare(org.qsardb.validation.Message left, org.qsardb.validation.Message right){
					return compareQdbPaths(left.getPath(), right.getPath());
				}
			};
			Collections.sort(messages, comparator);

			for(org.qsardb.validation.Message message : messages){
				Row messageRow = messagesTable.addRow(null, Row.ROLE_DATA, null);

				Cell imageCell = messageRow.addCell();
				switch(message.getLevel()){
					case ERROR:
						imageCell.addFigure(super.contextPath + "/static/icons/error.png", null, "icon"); // XXX
						break;
					case WARNING:
						imageCell.addFigure(super.contextPath + "/static/icons/warning.png", null, "icon"); // XXX
						break;
				}

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

	static
	private int compareQdbPaths(String left, String right){

		if((left).equals(right)){
			return 0;
		}

		String[] leftParts = parseQdbPath(left);
		String[] rightParts = parseQdbPath(right);

		if(leftParts.length > 0 && rightParts.length > 0){
			int diff = compareContainerRegistryPaths(leftParts[0], rightParts[0]);

			if(diff != 0){
				return diff;
			}
		}

		if(leftParts.length > 1 && rightParts.length > 1){
			int diff = compareContainerPaths(leftParts[1], rightParts[1]);

			if(diff != 0){
				return diff;
			}
		}


		if(leftParts.length > 2 && rightParts.length > 2){
			int diff = compareCargoPaths(leftParts[2], rightParts[2]);

			if(diff != 0){
				return diff;
			}
		}

		return (leftParts.length - rightParts.length);
	}

	static
	private String[] parseQdbPath(String path){
		return path.split("/");
	}

	static
	private int compareContainerRegistryPaths(String left, String right){
		int leftIndex = containerRegistries.indexOf(left.toLowerCase());
		int rightIndex = containerRegistries.indexOf(right.toLowerCase());

		return (leftIndex - rightIndex);
	}

	static
	private int compareContainerPaths(String left, String right){
		int diff = (left.length() - right.length());

		if(diff == 0){
			return comparePaths(left, right);
		}

		return diff;
	}

	static
	private int compareCargoPaths(String left, String right){
		return comparePaths(left, right);
	}


	static
	private int comparePaths(String left, String right){
		return (left.toLowerCase()).compareTo(right.toLowerCase());
	}

	private static final java.util.List<String> containerRegistries = Arrays.asList("compounds", "properties", "descriptors", "models", "predictions", "workflows");

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