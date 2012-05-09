package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.sql.*;
import java.util.*;

import org.dspace.app.xmlui.cocoon.*;
import org.dspace.app.xmlui.utils.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.*;
import org.dspace.browse.*;
import org.dspace.core.*;
import org.dspace.eperson.*;
import org.dspace.sort.*;

public class RepositoryRecentSubmissions extends AbstractDSpaceTransformer {

	@Override
	public void addBody(Body body) throws WingException, UIException, SQLException {
		Division home = body.addDivision("repository-home", "primary repository");

		java.util.List<BrowseItem> items = getRecentlySubmittedItems();
		if(items.size() == 0){
			return;
		}

		Division lastSubmittedDiv = home.addDivision("repository-recent-submission","secondary recent-submission");
		lastSubmittedDiv.setHead(T_head_recent_submissions);

		ReferenceSet lastSubmitted = lastSubmittedDiv.addReferenceSet("repository-last-submitted", ReferenceSet.TYPE_SUMMARY_LIST, null, "recent-submissions");
		for (BrowseItem item : items){
			lastSubmitted.addReference(item);
		}
	}

	private java.util.List<BrowseItem> getRecentlySubmittedItems() throws SQLException {
		String source = ConfigurationManager.getProperty("recent.submissions.sort-option");

		BrowserScope scope = new BrowserScope(super.context);
		scope.setAuthorityValue(source);
		scope.setResultsPerPage(10); // XXX

		try {
			scope.setBrowseIndex(BrowseIndex.getItemBrowseIndex());

			Set<SortOption> options = SortOption.getSortOptions();
			for(SortOption option : options){

				if((option.getName()).equals(source)){
					scope.setSortBy(option.getNumber());
					scope.setOrder(SortOption.DESCENDING);
				}
			}

			BrowseEngine browseEngine = new BrowseEngine(super.context);

			BrowseInfo info = browseEngine.browse(scope);

			java.util.List<BrowseItem> result = new ArrayList<BrowseItem>(info.getResults());

			items:
			for(Iterator<BrowseItem> it = result.iterator(); it.hasNext(); ){
				BrowseItem item = it.next();

				Group[] authorizedGroups = AuthorizeManager.getAuthorizedGroups(super.context, item, Constants.READ);
				for(Group authorizedGroup : authorizedGroups){

					if(authorizedGroup.getID() == 0){
						continue items;
					}
				}

				it.remove();
			}

			return result;
		} catch(BrowseException be){
			// Ignored
		} catch(SortException se){
			// Ignored
		}

		return Collections.emptyList();
	}

	private static final Message T_head_recent_submissions = message("xmlui.ArtifactBrowser.RepositoryViewer.head_recent_submissions");
}