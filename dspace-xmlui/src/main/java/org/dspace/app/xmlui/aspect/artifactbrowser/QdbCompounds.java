// TODO: paging for long compound lists ?

package org.dspace.app.xmlui.aspect.artifactbrowser;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.util.HashUtil;
import org.apache.excalibur.source.SourceValidity;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import java.util.LinkedHashMap;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Cell;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.content.Item;
import org.dspace.content.QdbCallable;
import org.dspace.content.QdbParameterUtil;
import org.dspace.content.QdbUtil;
import org.dspace.content.citation.ACSReferenceStyle;
import org.dspace.content.citation.ReferenceFormatter;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.Value;
import org.qsardb.cargo.bibtex.BibTeXCargo;
import org.qsardb.cargo.map.ReferencesCargo;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.cargo.structure.ChemicalMimeData;
import org.qsardb.model.Compound;
import org.qsardb.model.Model;
import org.qsardb.model.Parameter;
import org.qsardb.model.Prediction;
import org.qsardb.model.Property;
import org.qsardb.model.PropertyRegistry;
import org.qsardb.model.Qdb;

public class QdbCompounds extends ApplicationTransformer implements CacheableProcessingComponent {

	private static final String ID = "id";
	private static final String PROPERTY = "property";

	private SourceValidity validity;

	@Override
	protected Message getTitle(Item item){
		if(item == null){
			return T_trail;
		}

		return T_item_trail.parameterize(item.getHandle());
	}

	@Override
	public void addBody(Body body) throws SQLException, WingException, ProcessingException {
		Division div = body.addDivision("compounds");

		Item item = obtainItem();
		if(item == null || item.isWithdrawn()){
			return;
		}

		String title = ItemViewer.getItemTitle(item);
		if (title != null) {
			div.setHead(title);
		}

		String compoundId = getParameter(ID);
		if (compoundId == null) {
			process(item, div);
		} else {
			process(item, compoundId, div);
		}

	}

	public String process(Item item, final Division div) throws WingException, ProcessingException {
		final String propId = getParameter(PROPERTY);

		QdbCallable<String> callable = new QdbCallable<String>(){
			@Override
			public String call(Qdb qdb) throws Exception {
				Property property = qdb.getProperty(propId);
				Map<String, String> pvals = QdbParameterUtil.loadStringValues(property);

				Map<String, String> references = new LinkedHashMap<String, String>();
				if (property != null && property.hasCargo(ReferencesCargo.class)) {
					ReferencesCargo cargo = property.getCargo(ReferencesCargo.class);
					references = cargo.loadReferences();
				}
				Map<String, String> referenceNumbers = new LinkedHashMap<String, String>();

				ArrayList<String> cidList = new ArrayList<String>();
				for (Compound c: qdb.getCompoundRegistry()) {
					if (!pvals.isEmpty() && !pvals.containsKey(c.getId())) {
						continue;
					}
					cidList.add(c.getId());
				}

				Para para = div.addPara();
				para.addContent(cidList.size());
				para.addContent(" compounds");
				if (!pvals.isEmpty()) {
					para.addContent(" | Property ");
					para.addContent(property.getId()+": "+property.getName());
					QdbFormat.unit(property, para);
					QdbFormat.descriptionAttribute(property, para);
				}

				int nrows = cidList.size()+1;
				int ncols = pvals.isEmpty() ? 3 : 4;
				if (!references.isEmpty()) {
					ncols++;
				}
				Table table = div.addTable("compounds", nrows, ncols);

				Row header = table.addRow(Row.ROLE_HEADER);
				header.addCell(null, Cell.ROLE_HEADER, "short").addContent("ID");
				header.addCell().addContent("Name");
				if (!pvals.isEmpty()) {
					header.addCell(null, Cell.ROLE_HEADER, "short").addContent(propId);
				}
				if (!references.isEmpty()) {
					header.addCell(null, Cell.ROLE_HEADER, "short").addContent("Ref");
				}
				header.addCell(null, Cell.ROLE_HEADER, "short").addContent("Details");
				for (String cid: cidList) {
					Compound c = qdb.getCompound(cid);
					Row row = table.addRow();
					row.addCell().addContent(c.getId());
					row.addCell().addContent(c.getName());
					if (!pvals.isEmpty()) {
						row.addCell().addContent(pvals.get(c.getId()));
					}
					if (!references.isEmpty()) {
						row.addCell().addContent(fmtReference(references.get(cid), referenceNumbers));
					}
					row.addCell().addXref("?id="+c.getId(), "View");
				}

				if (property != null && property.hasCargo(BibTeXCargo.class)) {
					BibTeXCargo cargo = property.getCargo(BibTeXCargo.class);
					BibTeXDatabase db = cargo.loadBibTeX();
					Division biblio = div.addDivision("bibliography");
					biblio.setHead("Bibliography");
					List bibList = biblio.addList("bib-list", List.TYPE_ORDERED);
					ReferenceFormatter formatter = new ReferenceFormatter(new ACSReferenceStyle());

					Map<Key, BibTeXEntry> entries = new LinkedHashMap(db.getEntries());
					int n = 1;
					for (String k: referenceNumbers.keySet()) {
						BibTeXEntry e = entries.remove(new Key(k));
						String reference = formatter.format(e, true, true);
						bibList.addItem().addHtmlContent(reference);
					}

					for (Map.Entry<Key, BibTeXEntry> e: entries.entrySet()) {
						String reference = formatter.format(e.getValue(), true, true);
						bibList.addItem().addHtmlContent(reference);
					}

				}
				return "";
			}
		};
		try {
			return QdbUtil.invokeInternal(getContext(), item, callable);
		} catch (Exception ex) {
			throw new ProcessingException(ex.getMessage(), ex);
		}
	}

	private String fmtReference(String ref, Map<String, String> refs) {
		if (ref != null) {
			if (refs.containsKey(ref)) {
				return refs.get(ref);
			} else {
				refs.put(ref, "[" + (refs.size()+1) + "]");
				return refs.get(ref);
			}
		}
		return "";
	}

	public String process(Item item, final String compoundId, final Division div) throws WingException, ProcessingException {
		
		QdbCallable<String> callable = new QdbCallable<String>(){
			@Override
			public String call(Qdb qdb) throws Exception {
				Compound c = qdb.getCompound(compoundId);
				if (c == null) {
					div.addPara("Unknown compund: "+compoundId);
					return "";
				}

				int rows = 6;

				Division compDiv = div.addDivision("compound");
				compDiv.setHead("Compound");

				Table t = compDiv.addTable("test", rows, 3);
				Row idRow = t.addRow(Row.ROLE_HEADER);
				String url = depictionURL(c);
				idRow.addCell("depict", null, rows, 1, null).addFigure(url, null, null);
				idRow.addCell(null, Cell.ROLE_HEADER, "short").addContent("ID:");
				idRow.addCellContent(c.getId());

				Row nameRow = t.addRow();
				nameRow.addCellContent("Name:");
				nameRow.addCellContent(c.getName());

				Row descRow = t.addRow();
				descRow.addCellContent("Description:");
				descRow.addCellContent(fmt(c.getDescription()));

				Row labelsRow = t.addRow();
				labelsRow.addCellContent("Labels:");
				labelsRow.addCellContent(Joiner.on(", ").join(c.getLabels()));

				Row casRow = t.addRow();
				casRow.addCellContent("CAS:");
				casRow.addCellContent(fmt(c.getCas()));

				Row inchiRow = t.addRow();
				inchiRow.addCellContent("InChi Code:");
				inchiRow.addCellContent(fmt(c.getInChI()));

				generatePropertyInfo(qdb, c, div.addDivision("properties"));
				return "";
			}
		};
		try {
			return QdbUtil.invokeInternal(getContext(), item, callable);
		} catch (Exception ex) {
			throw new ProcessingException(ex.getMessage(), ex);
		}
	}

	private void generatePropertyInfo(Qdb qdb, Compound c, Division div) throws Exception {
		div.setHead("Properties");
		PropertyRegistry properties = qdb.getPropertyRegistry();

		if (properties.size() == 0) {
			div.addPara("This archive contains no properties");
		} else {
			for(Property property: properties){
				String pval = loadValue(property, c.getId());
				if (pval == null) {
					continue;
				}

				Para para = div.addPara("property-line", null);
				para.addContent(property.getId()+": "+property.getName());
				QdbFormat.unit(property, para);
				QdbFormat.descriptionAttribute(property, para);

				Table table = div.addTable("property-table", 1, 2);

				Row row = table.addRow();
				row.addCellContent(pval);
				row.addCell().addHtmlContent(loadPropertyReference(property, c.getId()));

				for (Model m: qdb.getModelRegistry().getByProperty(property)) {
					for (Prediction p: qdb.getPredictionRegistry().getByModel(m)) {
						pval = loadValue(p, c.getId());
						if (pval != null) {
							row = table.addRow();
							row.addCellContent(pval);
							row.addCell().addHtmlContent(m.getId()+": "+m.getName()+" ("+p.getName()+")");
						}
					}
				}
			}
		}
	}

	private String loadPropertyReference(Property p, String cid) throws Exception {
		Key referenceKey = null;
		if (p.hasCargo(ReferencesCargo.class)) {
			ReferencesCargo cargo = p.getCargo(ReferencesCargo.class);
			Map<String, String> references = cargo.loadReferences();
			if (references.containsKey(cid)) {
				referenceKey = new Key(references.get(cid));
			}
		}

		if (referenceKey != null && p.hasCargo(BibTeXCargo.class)) {
			BibTeXCargo cargo = p.getCargo(BibTeXCargo.class);
			Map<Key, BibTeXEntry> entries = cargo.loadBibTeX().getEntries();
			ReferenceFormatter formatter = new ReferenceFormatter(new ACSReferenceStyle());

			if (entries.containsKey(referenceKey)) {
				return formatter.format(entries.get(referenceKey), true, true);
			}
			if (entries.size() == 1) {
				return formatter.format(entries.values().iterator().next(), true, true);
			}
		}

		return "experimental value";
	}

	private String loadValue(Parameter<?,?> p, String cid) {
		Map<String, String> map = QdbParameterUtil.loadStringValues(p);
		return !map.isEmpty() ? map.get(cid) : null;
	}

	private String fmt(String value) {
		return value != null ? value : "";
	}

	private String getParameter(String name) {
		return ObjectModelHelper.getRequest(objectModel).getParameter(name);
	}

	public static String depictionURL(Compound c) {
		String smilesCargoID = ChemicalMimeData.DAYLIGHT_SMILES.getId(); 
		String structure = null;
		if (c.getInChI() != null) {
			structure = c.getInChI();
		} else if (c.hasCargo(smilesCargoID)) {
			try {
				structure = c.getCargo(smilesCargoID).loadString();
			} catch (IOException ex) {
				// ignored
			}
		} 
		
		if (structure == null) {
			structure = c.getName();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("https://cactus.nci.nih.gov/chemical/structure/");
		sb.append(structure.replace("#", "%23").replace("?", "%3f"));
		sb.append("/image").append("?format=png&crop=2&bgcolor=transparent");
		return sb.toString();
	}

	@Override
	public void recycle() {
		validity = null;
		super.recycle();
	}

	@Override
	public void dispose() {
		validity = null;
		super.dispose();
	}

	@Override
	public Serializable getKey() {
		try {
			Item item = obtainItem();
			if (item == null) {
				return "0";
			}

			StringBuilder sb = new StringBuilder();
			sb.append("compounds/").append(item.getHandle());
			sb.append(getParameter(PROPERTY));
			sb.append(getParameter(ID));
			return HashUtil.hash(sb.toString());
		} catch (SQLException ex) {
			return "0";
		}
	}

	@Override
	public SourceValidity getValidity() {
		if (this.validity == null) {
			try {
				DSpaceValidity newValidity = new DSpaceValidity();
				newValidity.add(obtainItem());
				this.validity =  newValidity.complete();
			}
			catch (Exception e) {
				// ignore
			}

		}
		return validity;
	}

	private static final Message T_trail = message("xmlui.ArtifactBrowser.QdbCompounds.trail");
	private static final Message T_item_trail = message("xmlui.ArtifactBrowser.QdbCompounds.handle_trail");
}
