/*
 *  Copyright (c) 2021 University of Tartu
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.content.Item;
import org.dspace.content.QmrfArchive;
import org.dspace.core.Context;

class QmrfContentPanel {

	static void generate(ItemViewer viewer, Item item, Division div) throws WingException {
		Context context = viewer.getContext();
		QmrfArchive qmrf = new QmrfArchive(context, item);

		Para para = div.addPara();
		if (qmrf.pdfLink().isEmpty() || qmrf.previewLink().isEmpty()) {
			return;
		}

		String pdfLink = viewer.getContextPath() + qmrf.pdfLink();
		String previewImage = viewer.getContextPath() + qmrf.previewLink();
		para.addFigure(previewImage, pdfLink, null);
	}
}
