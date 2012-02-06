package org.dspace.content;

import java.sql.*;
import java.util.*;

import org.qsardb.model.*;

import org.dspace.core.*;

public class QdbUtil {

	private QdbUtil(){
	}

	static
	public BitstreamFormat getBitstreamFormat(Context context) throws SQLException {
		BitstreamFormat format = BitstreamFormat.findByShortDescription(context, "QsarDB");
		if(format == null){
			throw new QdbConfigurationException("QsarDB bitstream format not found");
		}

		return format;
	}

	static
	public void collectMetadata(Item item, Qdb qdb){
		collectDublinCoreMetadata(item, qdb);
		collectQsarDBMetadata(item, qdb);
	}

	static
	public void collectDublinCoreMetadata(Item item, Qdb qdb){
		Archive archive = qdb.getArchive();

		item.addMetadata("dc", "title", null, null, archive.getName());
	}

	static
	public void collectQsarDBMetadata(Item item, Qdb qdb){
		List<String> endpointValues = new ArrayList<String>();
		List<String> speciesValues = new ArrayList<String>();

		PropertyRegistry properties = qdb.getPropertyRegistry();
		for(Property property : properties){
			String endpoint = property.getEndpoint();
			if(endpoint != null){
				endpointValues.add(endpoint);
			}

			String species = property.getSpecies();
			if(species != null){
				speciesValues.add(species);
			}
		}

		if(endpointValues.size() > 0){
			item.addMetadata("qdb", "property", "endpoint", null, toArray(endpointValues));
		} // End if

		if(speciesValues.size() > 0){
			item.addMetadata("qdb", "property", "species", null, toArray(speciesValues));
		}
	}

	static
	private String[] toArray(List<String> strings){
		return strings.toArray(new String[strings.size()]);
	}
}