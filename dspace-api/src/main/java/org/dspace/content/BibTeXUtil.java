package org.dspace.content;

import java.util.*;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;

import org.jbibtex.*;

import static org.jbibtex.BibTeXEntry.*;

public class BibTeXUtil {
	private static final ItemService itemService = ContentServiceFactory.getInstance().getItemService();

	private BibTeXUtil(){
	}

	static
	public BibTeXEntry toEntry(Item item){
		Key type = getType(item);
		if(type == null){
			return null;
		}

		Key key = new Key(String.valueOf(item.getID()));

		BibTeXEntry entry = new BibTeXEntry(type, key);

		Map<Key, Value> fields = getFields(item);
		entry.addAllFields(fields);

		return entry;
	}

	static
	public Key getType(Item item){

		List<MetadataValue> entries = itemService.getMetadata(item, "bibtex", "entry", null, Item.ANY);
		if(entries.size() == 1){
			return new Key(entries.get(0).getValue());
		}

		return null;
	}

	static
	public Map<Key, Value> getFields(Item item){
		KeyMap<Value> result = new KeyMap<Value>();

		List<MetadataValue> entries = itemService.getMetadata(item, "bibtex", "entry", Item.ANY, Item.ANY);
		for(MetadataValue entry : entries){

			// XXX
			if(entry.getMetadataField().getQualifier() == null || entry.getValue() == null){
				continue;
			} // End if

			Key key = new Key(entry.getMetadataField().getQualifier());

			Value value = new StringValue(entry.getValue(), StringValue.Style.BRACED);

			if(KEY_AUTHOR.equals(key) || KEY_EDITOR.equals(key)){
				Value previousValue = result.get(key);

				if(previousValue != null){
					value = new StringValue(previousValue.toUserString() + " and " + value.toUserString(), StringValue.Style.BRACED);
				}
			}

			result.put(key, value);
		}

		return result;
	}

	static
	public List<Key> getFields(Key type){

		if((TYPE_ARTICLE).equals(type)){
			return Arrays.asList(KEY_AUTHOR, KEY_TITLE, KEY_JOURNAL, KEY_YEAR, KEY_VOLUME, KEY_NUMBER, KEY_PAGES, KEY_DOI);
		} else

		if((TYPE_BOOK).equals(type)){
			return Arrays.asList(KEY_AUTHOR, KEY_TITLE, KEY_EDITION, KEY_EDITOR, KEY_SERIES, KEY_PUBLISHER, KEY_ADDRESS, KEY_YEAR, KEY_VOLUME, KEY_NUMBER, KEY_DOI);
		} else

		if((TYPE_INCOLLECTION).equals(type)){
			return Arrays.asList(KEY_AUTHOR, KEY_TITLE, KEY_BOOKTITLE, KEY_EDITION, KEY_EDITOR, KEY_SERIES, KEY_PUBLISHER, KEY_ADDRESS, KEY_YEAR, KEY_VOLUME, KEY_NUMBER, KEY_TYPE, KEY_CHAPTER, KEY_PAGES, KEY_DOI);
		} else

		if((TYPE_INPROCEEDINGS).equals(type)){
			return Arrays.asList(KEY_AUTHOR, KEY_TITLE, KEY_BOOKTITLE, KEY_EDITOR, KEY_SERIES, KEY_PUBLISHER, KEY_ADDRESS, KEY_YEAR, KEY_VOLUME, KEY_NUMBER, KEY_PAGES, KEY_ORGANIZATION, KEY_DOI);
		} else

		if((TYPE_UNPUBLISHED).equals(type)){
			return Arrays.asList(KEY_AUTHOR, KEY_TITLE, KEY_YEAR, KEY_URL);
		}

		return null;
	}

	static public BibTeXEntry toQsarDBEntry(Item item){
		Key key = new Key(item.getHandle());
		BibTeXEntry entry = new BibTeXEntry(TYPE_MISC, key);

		KeyMap<Value> keyMap = new KeyMap<Value>();

		StringBuilder sb = new StringBuilder();
		for (MetadataValue field: itemService.getMetadata(item, MetadataSchema.DC_SCHEMA, "publisher", Item.ANY, Item.ANY)){
			sb.append(" and ").append(field.getValue());
		}
		String authors = sb.substring(5);
		keyMap.put(KEY_AUTHOR, new StringValue(authors, StringValue.Style.BRACED));

		String handle = item.getHandle();
		String title = "QDB archive #" + handle.substring(handle.indexOf("/") + 1);
		keyMap.put(KEY_TITLE, new StringValue(title, StringValue.Style.BRACED));

		String howpublished = "QsarDB repository";
		keyMap.put(KEY_HOWPUBLISHED, new StringValue(howpublished, StringValue.Style.BRACED));

		for (MetadataValue field: itemService.getMetadata(item, MetadataSchema.DC_SCHEMA, "date", "issued", Item.ANY)){
			String year = field.getValue().split("-")[0];
			keyMap.put(KEY_YEAR, new StringValue(year, StringValue.Style.BRACED));
			break;
		}

		for (MetadataValue field: itemService.getMetadata(item, MetadataSchema.DC_SCHEMA, "identifier", "uri", Item.ANY)){
			String uri = field.getValue();
			keyMap.put(KEY_URL, new StringValue(uri, StringValue.Style.BRACED));
			if (uri.contains("doi.org")) {
				keyMap.remove(KEY_URL);
				String doi = uri.replace("http://dx.doi.org/", "");
				keyMap.put(KEY_DOI, new StringValue(doi, StringValue.Style.BRACED));
				break;
			}
		}

		entry.addAllFields(keyMap);

		return entry;
	}
}
