package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.sql.*;

import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.content.Item;

public class QdbExplorer extends ApplicationTransformer {

	@Override
	protected Message getTitle(Item item){

		if(item == null){
			return T_trail;
		}

		return T_item_trail.parameterize(item.getHandle());
	}

	@Override
	public void addPageMeta(PageMeta pageMeta) throws SQLException, WingException {
		super.addPageMeta(pageMeta);

		Metadata script = pageMeta.addMetadata("javascript", "static");
		script.addContent("static/org.dspace.gwt.Explorer/org.dspace.gwt.Explorer.nocache.js");
	}

	private static final Message T_trail = message("xmlui.ArtifactBrowser.QdbExplorer.trail");

	private static final Message T_item_trail = message("xmlui.ArtifactBrowser.QdbExplorer.handle_trail");
}