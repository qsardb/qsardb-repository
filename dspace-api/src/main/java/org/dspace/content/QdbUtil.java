package org.dspace.content;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.local.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.log4j.*;

import org.dspace.authorize.*;
import org.dspace.core.*;
import org.dspace.core.Context;
import org.dspace.content.citation.ACSAuthorFormat;
import org.dspace.content.citation.ACSReferenceStyle;
import org.dspace.content.citation.FieldFormat;
import org.dspace.content.citation.ReferenceFormatter;
import org.jbibtex.*;

import org.qsardb.cargo.bibtex.*;
import org.qsardb.cargo.map.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.rds.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;
import org.qsardb.storage.zipfile.*;


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
		Storage storage = getStorage(bitstream);

		try {
			Qdb qdb = new Qdb(storage);

			try {
				return callable.call(qdb);
			} finally {
				qdb.close();
			}
		} finally {
			storage.close();
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
	private BitstreamFormat getBitstreamFormat(Context context) throws SQLException {
		BitstreamFormat format = BitstreamFormat.findByShortDescription(context, "QsarDB");
		if(format == null){
			throw new QdbConfigurationException("No QsarDB bitstream format");
		}

		return format;
	}

	static
	private Storage getStorage(Bitstream bitstream) throws AuthorizeException, IOException, SQLException {
		GeneralFile file = bitstream.getFile();

		if(file instanceof LocalFile){
			LocalFile localFile = (LocalFile)file;

			if(localFile.isFile()){
				return new ZipFileInput(localFile.getFile());
			}
		}

		throw new IOException("Not a local file");
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
		BitstreamFormat format = getBitstreamFormat(context);

		List<Bitstream> bitstreams = getAllBitstreams(item, name, format);

		if(bitstreams.size() < 1){
			throw new QdbConfigurationException("No QsarDB bitstreams for name \"" + name + "\"");
		} else

		if(bitstreams.size() > 1){
			throw new QdbConfigurationException("Too many QsarDB bitstreams for name \"" + name + "\"");
		}

		return bitstreams.get(0);
	}

	static
	private List<Bitstream> getAllBitstreams(Item item, String name, BitstreamFormat format) throws SQLException {
		List<Bitstream> result = new ArrayList<Bitstream>();

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
	public Bitstream setOriginalBitstream(Context context, Item item, File file) throws AuthorizeException, IOException, SQLException {
		BitstreamData data = new FileBitstreamData(file);

		return setOriginalBitstream(context, item, data);
	}

	static
	public Bitstream setOriginalBitstream(Context context, Item item, BitstreamData data) throws AuthorizeException, IOException, SQLException {
		return setBitstream(context, item, ORIGINAL_BUNDLE_NAME, data);
	}

	static
	public Bitstream setInternalBitstream(Context context, Item item, File file) throws AuthorizeException, IOException, SQLException {
		BitstreamData data = new FileBitstreamData(file){

			@Override
			public String getName(){
				return "archive.qdb";
			}
		};

		return setInternalBitstream(context, item, data);
	}

	static
	public Bitstream setInternalBitstream(Context context, Item item, BitstreamData data) throws AuthorizeException, IOException, SQLException {
		return setBitstream(context, item, INTERNAL_BUNDLE_NAME, data);
	}

	static
	private Bitstream setBitstream(Context context, Item item, String name, BitstreamData data) throws AuthorizeException, IOException, SQLException {
		BitstreamFormat format = getBitstreamFormat(context);

		removeAllBitstreams(context, item, name, format);

		Bundle bundle = ensureBundle(item, name);

		InputStream is = data.getInputStream();

		try {
			Bitstream bitstream = bundle.createBitstream(is);
			bitstream.setFormat(format);
			bitstream.setName(data.getName());
			bitstream.setDescription(data.getDescription());
			bitstream.setSource(data.getSource());

			bitstream.update();

			bundle.setPrimaryBitstreamID(bitstream.getID());

			bundle.update();

			return bitstream;
		} finally {
			is.close();
		}
	}

	static
	private void removeAllBitstreams(Context context, Item item, String name, BitstreamFormat format) throws AuthorizeException, IOException, SQLException {
		List<Bitstream> bitstreams = getAllBitstreams(item, name, format);

		for(Bitstream bitstream : bitstreams){
			Bundle[] bundles = bitstream.getBundles();

			for(Bundle bundle : bundles){
				bundle.removeBitstream(bitstream);
			}
		}
	}

	static
	private Bundle ensureBundle(Item item, String name) throws AuthorizeException, IOException, SQLException {
		Bundle[] bundles = item.getBundles(name);

		if(bundles.length > 0){
			return bundles[0];
		}

		return item.createBundle(name);
	}

	static
	public File optimize(File file) throws QdbException, IOException {
		Storage storage = new ZipFileInput(file);

		try {
			return optimize(storage);
		} finally {
			storage.close();
		}
	}

	static
	public File optimize(Storage storage) throws QdbException, IOException {
		File result = createTempFile();

		Qdb qdb = new Qdb(storage);

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

		return result;
	}

	static
	public Evaluator getEvaluator(Model model) throws Exception {
		Qdb qdb = model.getQdb();

		if(model.hasCargo(PMMLCargo.class)){
			PMMLCargo pmmlCargo = model.getCargo(PMMLCargo.class);

			return new PMMLEvaluator(qdb, pmmlCargo.loadPmml());
		} else

		if(model.hasCargo(RDSCargo.class)){
			RDSCargo rdsCargo = model.getCargo(RDSCargo.class);

			return new QdbRDSEvaluator(qdb, rdsCargo.loadRdsObject());
		}

		throw new IllegalArgumentException();
	}

	static
	public File createTempFile() throws IOException {
		return File.createTempFile("bitstream", ".qdb");
	}

	static
	public void resetMetadata(Item item, Qdb qdb){
		clearMetadata(item);
		collectMetadata(item, qdb);
	}

	static
	private void clearMetadata(Item item){
		clearDcMetadata(item);
		clearBibTeXMetadata(item);
		clearQdbMetadata(item);
	}

	static
	private void collectMetadata(Item item, Qdb qdb){
		collectDcMetadata(item, qdb);
		collectBibTeXMetadata(item, qdb);
		collectQdbMetadata(item, qdb);
	}

	static
	private void clearDcMetadata(Item item){
		item.clearMetadata("dc", "description", "abstract", Item.ANY);
	}

	static
	private void collectDcMetadata(Item item, Qdb qdb){
		DCValue[] issued = item.getDC("date", "issued", Item.ANY);
		if (issued.length == 0) {
			item.addMetadata("dc", "date", "issued", null, "today");
		}

		String desc = qdb.getArchive().getDescription();
		if (desc != null) {
			item.addMetadata("dc", "description", "abstract", null, desc);
		}
	}

	static
	private void clearBibTeXMetadata(Item item){
		item.clearMetadata("bibtex", "entry", null, null);
		item.clearMetadata("bibtex", "entry", Item.ANY, null);
	}

	static
	private void collectBibTeXMetadata(Item item, Qdb qdb){
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
				string = ACSAuthorFormat.normalize(string);
				item.addMetadata("bibtex", "entry", (field.getValue()).toLowerCase(), null, string.split(" and "));
			} else

			{
				item.addMetadata("bibtex", "entry", (field.getValue()).toLowerCase(), null, string);
			}
		}
	}

	static
	private void clearQdbMetadata(Item item){
		item.clearMetadata("qdb", Item.ANY, Item.ANY, null);
	}

	static
	private void collectQdbMetadata(Item item, Qdb qdb){
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
			descriptorApplications.add(descriptor.getApplication());
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
			modelTypes.add(QdbModelUtil.detectType(model));
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
			predictionApplications.add(prediction.getApplication());
		}

		if(predictionApplications.size() > 0){
			item.addMetadata("qdb", "prediction", "application", null, predictionApplications.toArray());
		}
	}

	static
	public void resetTitle(Item item){
		clearTitle(item);
		addTitle(item);
	}

	static
	private void clearTitle(Item item){
		item.clearMetadata("dc", "title", null, null);
	}

	static
	private void addTitle(Item item){
		String reference = null;

		BibTeXEntry entry = BibTeXUtil.toEntry(item);
		if(entry != null){

			try {
				reference = formatReference(entry);
			} catch(Exception e){
				logger.error("Failed to create reference", e);
			}
		}

		item.addMetadata("dc", "title", null, null, reference);
	}

	static
	public BibTeXEntry getPopularEntry(Qdb qdb){
		FrequencyMap<BibTeXEntryHandle> map = new FrequencyMap<BibTeXEntryHandle>();

		PropertyRegistry props = qdb.getPropertyRegistry();
		collectEntries(map, props, 1);
		collectEntries(map, qdb.getModelRegistry(), 1+props.size());

		int maxCount = 0;
		BibTeXEntryHandle bestHandle = null;

		for(BibTeXEntryHandle handle : map.getKeys()){
			int count = map.getCount(handle);

			if (count > maxCount) {
				maxCount = count;
				bestHandle = handle;
			} else if (count == maxCount) {
				bestHandle = null;
			}
		}

		return bestHandle != null ? bestHandle.getEntry() : null;
	}

	static
	private void collectEntries(FrequencyMap<BibTeXEntryHandle> map, java.util.Collection<? extends Container<?, ?>> containers, int weight){

		for(Container<?, ?> container : containers){

			if(!container.hasCargo(BibTeXCargo.class)){
				continue;
			}

			BibTeXCargo cargo = container.getCargo(BibTeXCargo.class);

			try {
				BibTeXDatabase database = cargo.loadBibTeX();

				for(BibTeXEntry entry : database.getEntries().values()){
					for (int i=0; i<weight; i++) {
						map.add(new BibTeXEntryHandle(entry));
					}

					// Keep the first entry only
					break;
				}
			} catch(Exception e){
				continue;
			}
		}
	}

	static
	private String toString(Value value){
		if (value == null) {
			return null;
		}

		return FieldFormat.userString(value, true);
	}

	static
	private String formatReference(BibTeXEntry entry){
		ReferenceFormatter formatter = new ReferenceFormatter(new ACSReferenceStyle());

		// Don't want to have the DOI as part of the title
		entry.removeField(BibTeXEntry.KEY_DOI);

		return formatter.format(entry, false, false);
	}

	static
	public class ValueCollector {

		private Set<String> values = new LinkedHashSet<String>();


		public int size(){
			return this.values.size();
		}

		public void add(String value){

			if(value == null || value.trim().isEmpty()){
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

	private static final Logger logger = Logger.getLogger(QdbUtil.class);
}
