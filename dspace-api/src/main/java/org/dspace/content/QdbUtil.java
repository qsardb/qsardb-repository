package org.dspace.content;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.qsardb.model.*;
import org.qsardb.storage.zipfile.*;

import org.dspace.authorize.*;
import org.dspace.core.*;

public class QdbUtil {

	private QdbUtil(){
	}

	static
	public <X> X invokeOriginal(Context context, Item item, QdbCallable<X> callable) throws Exception {
		Bitstream bitstream = getOriginalBitstream(context, item);

		return invoke(bitstream, callable);
	}

	static
	public <X> X invokeInternal(Context context, Item item, QdbCallable<X> callable) throws Exception {
		Bitstream bitstream;

		try {
			bitstream = getInternalBitstream(context, item);
		} catch(QdbConfigurationException qce){

			if(QdbUtil.SAFE_MODE){
				bitstream = getOriginalBitstream(context, item);
			} else

			{
				throw qce;
			}
		}

		return invoke(bitstream, callable);
	}

	static
	private <X> X invoke(Bitstream bitstream, QdbCallable<X> callable) throws Exception {
		File file = loadFile(bitstream);

		try {
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
	public File loadFile(Bitstream bitstream) throws AuthorizeException, SQLException, IOException {
		File file = createTempFile();

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

		return file;
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
	public Bitstream getOriginalBitstream(Context context, Item item) throws SQLException {
		return getBitstream(context, item, ORIGINAL_BUNDLE_NAME);
	}

	static
	public Bitstream getInternalBitstream(Context context, Item item) throws SQLException {
		return getBitstream(context, item, INTERNAL_BUNDLE_NAME);
	}

	static
	private Bitstream getBitstream(Context context, Item item, String name) throws SQLException {
		List<Bitstream> bitstreams = getAllBitstreams(context, item, name);

		if(bitstreams.size() < 1){
			throw new QdbConfigurationException("No QsarDB bitstreams");
		} else

		if(bitstreams.size() > 1){
			throw new QdbConfigurationException("Too many QsarDB bitstreams");
		}

		return bitstreams.get(0);
	}

	static
	private List<Bitstream> getAllBitstreams(Context context, Item item, String name) throws SQLException {
		List<Bitstream> result = new ArrayList<Bitstream>();

		BitstreamFormat format = getBitstreamFormat(context);

		Bundle[] bundles = item.getBundles(name);
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
	public Bitstream addOriginalBitstream(Context context, Item item, BitstreamData data) throws AuthorizeException, SQLException, IOException {
		return addBitstream(context, item, ORIGINAL_BUNDLE_NAME, data);
	}

	static
	public Bitstream addInternalBitstream(Context context, Item item, BitstreamData data) throws AuthorizeException, SQLException, IOException {
		return addBitstream(context, item, INTERNAL_BUNDLE_NAME, data);
	}

	static
	private Bitstream addBitstream(Context context, Item item, String name, BitstreamData data) throws AuthorizeException, SQLException, IOException {
		BitstreamFormat format = getBitstreamFormat(context);

		Bundle bundle;

		Bundle[] bundles = item.getBundles(name);
		if(bundles != null && bundles.length > 1){
			bundle = bundles[0];
		} else

		{
			bundle = item.createBundle(name);
		}

		InputStream is = data.getInputStream();

		try {
			Bitstream bitstream = bundle.createBitstream(is);
			bitstream.setName(data.getName());
			bitstream.setDescription(data.getDescription());
			bitstream.setSource(data.getSource());
			bitstream.setFormat(format);

			bitstream.update();

			bundle.update();

			return bitstream;
		} finally {
			is.close();
		}
	}

	static
	public File optimize(File file) throws QdbException, IOException {
		File result = createTempFile();

		ZipFileInput input = new ZipFileInput(file);

		try {
			Qdb qdb = new Qdb(input);

			try {
				ZipFileOutput output = new ZipFileOutput(result);
				output.setLevel(0);

				try {
					qdb.copyTo(output);
				} finally {
					output.close();
				}
			} finally {
				qdb.close();
			}
		} finally {
			input.close();
		}

		return result;
	}

	static
	private File createTempFile() throws IOException {
		return File.createTempFile("bitstream", ".qdb");
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

	static
	public interface BitstreamData {

		String getName();

		String getDescription();

		String getSource();

		InputStream getInputStream() throws IOException;
	}

	static
	public class FileBitstreamData implements BitstreamData {

		private File file = null;


		public FileBitstreamData(File file){

			if(file == null){
				throw new NullPointerException();
			}

			this.file = file;
		}

		@Override
		public String getName(){
			return this.file.getName();
		}

		@Override
		public String getDescription(){
			return null;
		}

		@Override
		public String getSource(){
			return this.file.getName();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new FileInputStream(this.file);
		}
	}

	private static final boolean SAFE_MODE = true;

	private static final String ORIGINAL_BUNDLE_NAME = Constants.DEFAULT_BUNDLE_NAME;

	private static final String INTERNAL_BUNDLE_NAME = "INTERNAL";
}