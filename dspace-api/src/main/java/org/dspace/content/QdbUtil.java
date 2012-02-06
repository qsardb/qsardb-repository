package org.dspace.content;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.qsardb.model.*;
import org.qsardb.storage.zipfile.*;

import org.dspace.core.*;

public class QdbUtil {

	private QdbUtil(){
	}

	static
	public BitstreamFormat getBitstreamFormat(Context context) throws SQLException {
		BitstreamFormat format = BitstreamFormat.findByShortDescription(context, "QsarDB");
		if(format == null){
			throw new QdbConfigurationException("No QsarDB bitstream format");
		}

		return format;
	}

	static
	public Bitstream getBitstream(Context context, Item item) throws SQLException {
		List<Bitstream> bitstreams = getAllBitstreams(context, item);
		if(bitstreams.size() < 1){
			throw new QdbConfigurationException("No QsarDB bitstreams");
		} else

		if(bitstreams.size() > 1){
			throw new QdbConfigurationException("Too many QsarDB bitstreams");
		}

		return bitstreams.get(0);
	}

	static
	public List<Bitstream> getAllBitstreams(Context context, Item item) throws SQLException {
		BitstreamFormat format = getBitstreamFormat(context);

		List<Bitstream> result = new ArrayList<Bitstream>();

		Bundle[] bundles = item.getBundles(Constants.DEFAULT_BUNDLE_NAME);
		for(Bundle bundle : bundles){
			Bitstream[] bitstreams = bundle.getBitstreams();

			for(Bitstream bitstream : bitstreams){

				if((bitstream.getFormat()).getID() == format.getID()){
					result.add(bitstream);
				}
			}
		}

		return result;
	}

	static
	public <X> X invoke(Context context, Item item, QdbCallable<X> callable) throws Exception {
		Bitstream bitstream = getBitstream(context, item);

		return invoke(bitstream, callable);
	}

	static
	public <X> X invoke(Bitstream bitstream, QdbCallable<X> callable) throws Exception {
		File file = File.createTempFile("bitstream", ".qdb");

		try {
			InputStream is = bitstream.retrieve();

			try {
				OutputStream os = new FileOutputStream(file);

				try {
					Utils.copy(is, os);

					os.flush();
				} finally {
					os.close();
				}
			} finally {
				is.close();
			}

			Qdb qdb = new Qdb(new ZipFileInput(file));

			try {
				return callable.call(qdb);
			} finally {
				qdb.close();
			}
		} finally {
			file.delete();
		}
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