package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.view.client.*;

import org.dspace.gwt.rpc.*;

public class CompoundDataProvider extends ListDataProvider<Compound> implements SeriesDisplayEventHandler {

	private List<Compound> compounds = null;


	public CompoundDataProvider(List<Compound> compounds){
		super(compounds);

		setCompounds(compounds);
	}

	@Override
	public void onVisibilityChanged(SeriesDisplayEvent event){
		Set<String> visibleIds = new LinkedHashSet<String>();

		Set<PredictionColumn> visiblePredictions = event.getValues(Boolean.TRUE);
		for(PredictionColumn visiblePrediction : visiblePredictions){
			visibleIds.addAll((visiblePrediction.getValues()).keySet());
		}

		List<Compound> visibleCompounds = new ArrayList<Compound>();

		List<Compound> compounds = getCompounds();
		for(Compound compound : compounds){
			String id = compound.getId();

			if(visibleIds.contains(id)){
				visibleCompounds.add(compound);
			}
		}

		setList(visibleCompounds);
	}

	public List<Compound> getCompounds(){
		return this.compounds;
	}

	private void setCompounds(List<Compound> compounds){
		this.compounds = compounds;
	}

	static
	public List<Compound> format(Collection<String> ids){
		List<Compound> result = new ArrayList<Compound>();

		for(String id : ids){
			Compound compound = new Compound();
			compound.setId(id);

			result.add(compound);
		}

		return result;
	}
}