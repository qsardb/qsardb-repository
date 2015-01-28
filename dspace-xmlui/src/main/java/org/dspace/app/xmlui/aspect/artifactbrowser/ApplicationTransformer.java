package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.sql.*;
import org.apache.cocoon.ProcessingException;

import org.dspace.app.xmlui.cocoon.*;
import org.dspace.app.xmlui.utils.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.content.*;
import org.dspace.content.Item;

abstract
public class ApplicationTransformer extends AbstractDSpaceTransformer {

	abstract
	protected Message getTitle(Item item);

	@Override
	public void addPageMeta(PageMeta pageMeta) throws SQLException, WingException {
		Item item = obtainItem();
		if(item == null || item.isWithdrawn()){
			return;
		}

		Metadata title = pageMeta.addMetadata("title");
		title.addContent(getTitle(item));

		Metadata gwt = pageMeta.addMetadata("gwt", "historyFrame");
		gwt.addContent("true"); // XXX

		pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
		HandleUtil.buildHandleTrail(item, pageMeta, contextPath);
		pageMeta.addTrailLink(contextPath + "/handle/" + item.getHandle(), item.getHandle());

		Trail trail = pageMeta.addTrail();
		trail.addContent(getTitle(null));
	}

	@Override
	public void addBody(Body body) throws SQLException, WingException, ProcessingException {
		Item item = obtainItem();
		if(item == null || item.isWithdrawn()){
			return;
		}

		Division division = body.addDivision("main", "primary");
	}

	protected Item obtainItem() throws SQLException {
		DSpaceObject object = HandleUtil.obtainHandle(objectModel);
		if(!(object instanceof Item)){
			return null;
		}

		return (Item)object;
	}

    private static final Message T_dspace_home = message("xmlui.general.dspace_home");
}