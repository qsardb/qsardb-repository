package org.dspace.qsardb.client.gwt;

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import java.util.Map;

public class ApplicabilityDomainTooltip extends Tooltip {

	ApplicabilityDomainTooltip(Map<String, String> details) {
		init(details);
	}

	private void init(Map<String, String> details) {
		FlowPanel panel = new FlowPanel();
		panel.getElement().getStyle().setTextAlign(Style.TextAlign.LEFT);

		Label title = new Label("Applicability Domain details:");
		title.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
		title.getElement().getStyle().setPaddingBottom(6, Style.Unit.PX);
		panel.add(title);

		add(panel, details.get("descriptors").equals("YES"), "Descriptor domain");
		add(panel, details.get("analogues").equals("YES"), "Structural domain");
		add(panel, details.get("responses").equals("YES"), "Response domain");

		setWidget(panel);
	}

	private void add(FlowPanel panel, boolean value, String label) {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		if (value) {
			sb.appendHtmlConstant("<i class=\"far fa-check-square\"></i> ");
		} else {
			sb.appendHtmlConstant("<i class=\"far fa-square\"></i> ");
		}
		sb.appendEscaped(label);
		sb.appendHtmlConstant("</input>");
		HTML html = new HTML(sb.toSafeHtml());
		html.getElement().getStyle().setPaddingBottom(3, Style.Unit.PX);
		panel.add(html);
	}

	@Override
	protected void render() {
		setPopupPositionAndShow(getPositionCallback());
	}
}