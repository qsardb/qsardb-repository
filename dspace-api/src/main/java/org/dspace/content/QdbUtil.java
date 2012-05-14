package org.dspace.content;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.qsardb.cargo.bibtex.*;
import org.qsardb.cargo.map.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;
import org.qsardb.storage.zipfile.*;

import org.jbibtex.*;

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
	public Evaluator getEvaluator(Model model) throws Exception {
		EvaluatorFactory evaluatorFactory = EvaluatorFactory.getInstance();
		evaluatorFactory.setActivating(false);

		return evaluatorFactory.getEvaluator(model);
	}

	static
	private File createTempFile() throws IOException {
		return File.createTempFile("bitstream", ".qdb");
	}

	static
	public void collectMetadata(Item item, Qdb qdb){
		collectBibTeXMetadata(item, qdb);
		collectQDBMetadata(item, qdb);
	}

	static
	public void collectBibTeXMetadata(Item item, Qdb qdb){
		BibTeXEntry entry = getPopularEntry(qdb);

		if(entry == null){
			return;
		}

		Key type = entry.getType();

		Set<Key> types = ENTRY_TYPES;
		if(!types.contains(type)){
			return;
		}

		item.addMetadata("bibtex", "entry", null, null, (type.getValue()).toLowerCase());

		Set<Key> fields = ENTRY_FIELDS;
		for(Key field : fields){
			Value value = entry.getField(field);

			if(value == null){
				continue;
			}

			String string = toString(value);

			if(string == null){
				continue;
			} // End if

			if((BibTeXEntry.KEY_AUTHOR).equals(field) || (BibTeXEntry.KEY_EDITOR).equals(field)){
				item.addMetadata("bibtex", "entry", (field.getValue()).toLowerCase(), null, string.split(" and "));
			} else

			{
				item.addMetadata("bibtex", "entry", (field.getValue()).toLowerCase(), null, string);
			}
		}
	}

	static
	public void collectQDBMetadata(Item item, Qdb qdb){
		collectPropertyMetadata(item, qdb);
		collectDescriptorMetadata(item, qdb);
		collectModelMetadata(item, qdb);
		collectPredictionMetadata(item, qdb);
	}

	static
	private void collectPropertyMetadata(Item item, Qdb qdb){
		ValueCollector propertyEndpoints = new ValueCollector();
		ValueCollector propertySpecies = new ValueCollector();

		PropertyRegistry properties = qdb.getPropertyRegistry();
		for(Property property : properties){
			propertyEndpoints.add(property.getEndpoint());
			propertySpecies.add(property.getSpecies());
		}

		if(propertyEndpoints.size() > 0){
			item.addMetadata("qdb", "property", "endpoint", null, propertyEndpoints.toArray());
		} // End if

		if(propertySpecies.size() > 0){
			item.addMetadata("qdb", "property", "species", null, propertySpecies.toArray());
		}
	}

	static
	private void collectDescriptorMetadata(Item item, Qdb qdb){
		ValueCollector descriptorApplications = new ValueCollector();

		DescriptorRegistry descriptors = qdb.getDescriptorRegistry();
		for(Descriptor descriptor : descriptors){
			descriptorApplications.add(formatApplication(descriptor.getApplication()));
		}

		if(descriptorApplications.size() > 0){
			item.addMetadata("qdb", "descriptor", "application", null, descriptorApplications.toArray());
		}
	}

	static
	private void collectModelMetadata(Item item, Qdb qdb){
		ValueCollector modelTypes = new ValueCollector();

		ModelRegistry models = qdb.getModelRegistry();
		for(Model model : models){
			modelTypes.add(loadType(model));
		}

		if(modelTypes.size() > 0){
			item.addMetadata("qdb", "model", "type", null, modelTypes.toArray());
		}
	}

	static
	private void collectPredictionMetadata(Item item, Qdb qdb){
		ValueCollector predictionApplications = new ValueCollector();

		PredictionRegistry predictions = qdb.getPredictionRegistry();
		for(Prediction prediction : predictions){
			predictionApplications.add(formatApplication(prediction.getApplication()));
		}

		if(predictionApplications.size() > 0){
			item.addMetadata("qdb", "prediction", "application", null, predictionApplications.toArray());
		}
	}

	static
	public BibTeXEntry getPopularEntry(Qdb qdb){
		FrequencyMap<BibTeXEntryHandle> map = new FrequencyMap<BibTeXEntryHandle>();

		collectEntries(map, qdb.getPropertyRegistry());
		collectEntries(map, qdb.getModelRegistry());

		int maxCount = 0;

		Set<BibTeXEntryHandle> maxHandles = new LinkedHashSet<BibTeXEntryHandle>();

		java.util.Collection<BibTeXEntryHandle> handles = map.getKeys();
		for(BibTeXEntryHandle handle : handles){
			int count = map.getCount(handle);

			if(count < maxCount){
				continue;
			} else

			if(count == maxCount){
				maxHandles.add(handle);
			} else

			if(count > maxCount){
				maxCount = count;

				maxHandles.clear();
				maxHandles.add(handle);
			}
		}

		if(maxHandles.size() == 1){
			BibTeXEntryHandle maxHandle = (maxHandles.iterator()).next();

			return maxHandle.getEntry();
		}

		return null;
	}

	static
	private void collectEntries(FrequencyMap<BibTeXEntryHandle> map, java.util.Collection<? extends Container<?, ?>> containers){

		for(Container<?, ?> container : containers){

			if(!container.hasCargo(BibTeXCargo.class)){
				continue;
			}

			BibTeXCargo cargo = container.getCargo(BibTeXCargo.class);

			try {
				BibTeXDatabase database = cargo.loadBibTeX();

				java.util.Collection<BibTeXEntry> entries = (database.getEntries()).values();
				for(BibTeXEntry entry : entries){
					map.add(new BibTeXEntryHandle(entry));

					// Keep the first entry only
					break;
				}
			} catch(Exception e){
				continue;
			}
		}
	}

	static
	private String loadType(Model model){

		try {
			Evaluator evaluator = getEvaluator(model);

			if(evaluator != null){
				evaluator.init();

				try {
					return evaluator.getSummary();
				} finally {
					evaluator.destroy();
				}
			}
		} catch(Exception e){
			// Ignored
		}

		return null;
	}

	static
	private String formatApplication(Application application){
		return (application != null ? application.getName() : null);
	}

	static
	private List<LaTeXObject> parseLaTeX(String string) throws IOException, ParseException {
		Reader reader = new StringReader(string);

		try {
			LaTeXParser parser = new LaTeXParser();

			return parser.parse(reader);
		} finally {
			reader.close();
		}
	}

	static
	private String printLaTeX(List<LaTeXObject> objects){
		LaTeXPrinter printer = new LaTeXPrinter();

		return printer.print(objects);
	}

	static
	private String toString(Value value){

		try {
			if(value != null){
				return printLaTeX(parseLaTeX(value.toUserString()));
			}
		} catch(Exception e){
			// Ignored
		}

		return null;
	}

	static
	public class ValueCollector {

		private Set<String> values = new LinkedHashSet<String>();


		public int size(){
			return this.values.size();
		}

		public void add(String value){

			if(value == null){
				return;
			}

			this.values.add(value);
		}

		public String[] toArray(){
			return this.values.toArray(new String[this.values.size()]);
		}
	}

	static
	private class BibTeXEntryHandle {

		private BibTeXEntry entry = null;


		private BibTeXEntryHandle(BibTeXEntry entry){
			setEntry(entry);
		}

		public Key getKey(){
			return getEntry().getKey();
		}

		@Override
		public int hashCode(){
			return getKey().hashCode();
		}

		@Override
		public boolean equals(Object object){

			if(object instanceof BibTeXEntryHandle){
				BibTeXEntryHandle that = (BibTeXEntryHandle)object;

				return (this.getKey()).equals(that.getKey());
			}

			return false;
		}

		public BibTeXEntry getEntry(){
			return this.entry;
		}

		private void setEntry(BibTeXEntry entry){
			this.entry = entry;
		}
	}

	private static final Set<Key> ENTRY_TYPES = new LinkedHashSet<Key>();

	static {
		ENTRY_TYPES.add(BibTeXEntry.TYPE_ARTICLE);
		ENTRY_TYPES.add(BibTeXEntry.TYPE_BOOK);
		ENTRY_TYPES.add(BibTeXEntry.TYPE_INCOLLECTION);
		ENTRY_TYPES.add(BibTeXEntry.TYPE_INPROCEEDINGS);
		ENTRY_TYPES.add(BibTeXEntry.TYPE_MISC);
		ENTRY_TYPES.add(BibTeXEntry.TYPE_UNPUBLISHED);
	}

	private static final Set<Key> ENTRY_FIELDS = new LinkedHashSet<Key>();

	static {
		ENTRY_FIELDS.add(BibTeXEntry.KEY_ADDRESS);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_ANNOTE);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_AUTHOR);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_BOOKTITLE);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_CHAPTER);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_DOI);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_EDITION);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_EDITOR);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_EPRINT);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_HOWPUBLISHED);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_INSTITUTION);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_JOURNAL);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_KEY);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_MONTH);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_NOTE);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_NUMBER);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_ORGANIZATION);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_PAGES);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_PUBLISHER);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_SCHOOL);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_SERIES);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_TITLE);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_TYPE);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_URL);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_VOLUME);
		ENTRY_FIELDS.add(BibTeXEntry.KEY_YEAR);
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