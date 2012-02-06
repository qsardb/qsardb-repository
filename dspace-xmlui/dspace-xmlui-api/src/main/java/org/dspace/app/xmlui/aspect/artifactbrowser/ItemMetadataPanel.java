package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.sql.*;
import java.util.*;

import org.apache.cocoon.environment.*;

import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.content.Collection;
import org.dspace.content.Item;

class ItemMetadataPanel {

	private ItemMetadataPanel(){
	}

	static
	void generate(ItemViewer viewer, Item item, Division division) throws SQLException, WingException {
        Map objectModel = viewer.getObjectModel();
        String contextPath = viewer.getContextPath();

        Para showfullPara = division.addPara(null, "item-view-toggle item-view-toggle-top");

        if (showFullItem(objectModel))
        {
            String link = contextPath + "/handle/" + item.getHandle();
            showfullPara.addXref(link).addContent(T_show_simple);
        }
        else
        {
            String link = contextPath + "/handle/" + item.getHandle()
                    + "?show=full";
            showfullPara.addXref(link).addContent(T_show_full);
        }

        ReferenceSet referenceSet;
        if (showFullItem(objectModel))
        {
            referenceSet = division.addReferenceSet("collection-viewer",
                    ReferenceSet.TYPE_DETAIL_VIEW);
        }
        else
        {
            referenceSet = division.addReferenceSet("collection-viewer",
                    ReferenceSet.TYPE_SUMMARY_VIEW);
        }

        // Refrence the actual Item
        ReferenceSet appearsInclude = referenceSet.addReference(item).addReferenceSet(ReferenceSet.TYPE_DETAIL_LIST,null,"hierarchy");
        appearsInclude.setHead(T_head_parent_collections);

        // Reference all collections the item appears in.
        for (Collection collection : item.getCollections())
        {
            appearsInclude.addReference(collection);
        }

        showfullPara = division.addPara(null,"item-view-toggle item-view-toggle-bottom");

        if (showFullItem(objectModel))
        {
            String link = contextPath + "/handle/" + item.getHandle();
            showfullPara.addXref(link).addContent(T_show_simple);
        }
        else
        {
            String link = contextPath + "/handle/" + item.getHandle()
                    + "?show=full";
            showfullPara.addXref(link).addContent(T_show_full);
        }
	}

    /**
     * Determine if the full item should be referenced or just a summary.
     */
    public static boolean showFullItem(Map objectModel)
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String show = request.getParameter("show");

        if (show != null && show.length() > 0)
        {
            return true;
        }

        return false;
    }

    private static final Message T_show_simple = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.show_simple");

    private static final Message T_show_full = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.show_full");

    private static final Message T_head_parent_collections = ItemViewer.message("xmlui.ArtifactBrowser.ItemViewer.head_parent_collections");
}