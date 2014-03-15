package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.aspect.submission.submit.QdbValidateStep;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Trail;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.submit.step.ItemMessageCollector;
import org.dspace.submit.step.QdbValidation;
import org.xml.sax.SAXException;

public class QdbValidator extends AbstractDSpaceTransformer {

    private static final Message T_dspace_home = message("xmlui.general.dspace_home");

	@Override
	public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
		Item item = getItem();
		if (item == null) {
			return;
		}
		
		String title = "Validation";
		pageMeta.addMetadata("title").addContent(title);

		pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
		HandleUtil.buildHandleTrail(item, pageMeta, contextPath);
		pageMeta.addTrailLink(contextPath + "/handle/" + item.getHandle(), item.getHandle());

		Trail trail = pageMeta.addTrail();
		trail.addContent(title);
	}

	@Override
	public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException, ProcessingException {
		Item item = getItem();
		if (item == null) {
			return;
		}

		Request request = ObjectModelHelper.getRequest(objectModel);
		String level = (String)request.get("level");

		ArrayList<org.qsardb.validation.Message> messages = new ArrayList<org.qsardb.validation.Message>();
		QdbValidation validation = new QdbValidation(level);
		ItemMessageCollector collector = validation.validate(context, item);
		messages.addAll(collector.getMessages());

		String action = contextPath + "/validate/" + item.getHandle();
		Division div = body.addInteractiveDivision("submit-validate", action, Division.METHOD_GET, "primary submission");
		QdbValidateStep.renderValidationForm(div, messages, level, contextPath);
	}

	private Item getItem() throws SQLException {
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (dso instanceof Item) {
            return (Item)dso;
        }
        return null;
	}
	
}