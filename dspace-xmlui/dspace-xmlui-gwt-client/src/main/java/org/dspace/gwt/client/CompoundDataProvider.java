package org.dspace.gwt.client;

import java.util.*;

import com.google.gwt.view.client.*;

public class CompoundDataProvider extends ListDataProvider<Compound> {

	public CompoundDataProvider(List<Compound> compounds){
		super(compounds);
	}

	static
	public List<Compound> format(Collection<String> ids){
		List<Compound> result = new ArrayList<Compound>(ids.size());

		for(String id : ids){
			Compound compound = new Compound();
			compound.setId(id);

			result.add(compound);
		}

		return result;
	}
}