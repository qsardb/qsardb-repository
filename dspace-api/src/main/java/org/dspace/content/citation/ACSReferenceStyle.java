/*
 * Copyright (c) 2012 University of Tartu
 */
package org.dspace.content.citation;

import java.util.*;

import org.jbibtex.*;

public class ACSReferenceStyle extends ReferenceStyle {

	public ACSReferenceStyle(){
		addFormat(BibTeXEntry.TYPE_ARTICLE, createArticleFormat());
		addFormat(BibTeXEntry.TYPE_BOOK, createBookFormat());
		addFormat(BibTeXEntry.TYPE_INCOLLECTION, createInCollectionFormat());
		addFormat(BibTeXEntry.TYPE_INPROCEEDINGS, createInProceedingsFormat());
		addFormat(BibTeXEntry.TYPE_MISC, createMiscFormat());
		addFormat(BibTeXEntry.TYPE_UNPUBLISHED, createUnpublishedFormat());
	}

	static
	private EntryFormat createArticleFormat(){
		List<FieldFormat> fields = Arrays.asList(
			new ACSAuthorFormat(null),
			new FieldFormat(BibTeXEntry.KEY_TITLE, "."),
			new JournalFormat(null),
			new YearFormat(","),
			new VolumeFormat(","),
			new FieldFormat(BibTeXEntry.KEY_PAGES, "."),
			new DOIFormat(null)
		);

		return new EntryFormat(fields);
	}

	static
	private EntryFormat createBookFormat(){
		List<FieldFormat> fields = Arrays.asList(
			new ACSAuthorFormat(null),
			new BookTitleFormat(";"),
			new EditorFormat(";"),
			new FieldFormat(BibTeXEntry.KEY_PUBLISHER, ":"),
			new FieldFormat(BibTeXEntry.KEY_ADDRESS, ";"),
			new YearFormat("."),
			new DOIFormat(null)
		);

		return new EntryFormat(fields);
	}

	static
	private EntryFormat createInCollectionFormat(){
		List<FieldFormat> fields = Arrays.asList(
			new ACSAuthorFormat(null),
			new FieldFormat(BibTeXEntry.KEY_TITLE, "."),
			new InBookTitleFormat(";"),
			new EditorFormat(";"),
			new FieldFormat(BibTeXEntry.KEY_PUBLISHER, ";"),
			new YearFormat("."),
			new DOIFormat(null)
		);

		return new EntryFormat(fields);
	}

	static
	private EntryFormat createInProceedingsFormat(){
		List<FieldFormat> fields = Arrays.asList(
			new ACSAuthorFormat(null),
			new FieldFormat(BibTeXEntry.KEY_TITLE, "."),
			new InBookTitleFormat(";"),
			new EditorFormat(";"),
			new FieldFormat(BibTeXEntry.KEY_ORGANIZATION, ";"),
			new YearFormat("."),
			new DOIFormat(null)
		);

		return new EntryFormat(fields);
	}

	static
	private EntryFormat createMiscFormat(){
		List<FieldFormat> fields = Arrays.asList(
			new ACSAuthorFormat(null),
			new FieldFormat(BibTeXEntry.KEY_TITLE, "."),
			new FieldFormat(BibTeXEntry.KEY_HOWPUBLISHED, ","),
			new YearFormat("."),
			new URLFormat(null),
			new DOIFormat(null)
		);

		return new EntryFormat(fields);
	}

	static
	private EntryFormat createUnpublishedFormat(){
		List<FieldFormat> fields = Arrays.asList(
			new ACSAuthorFormat(null),
			new FieldFormat(BibTeXEntry.KEY_TITLE, ".")
		);

		return new EntryFormat(fields);
	}

	static
	private String bold(String string, boolean html){

		if(html){
			string = ("<b>" + string + "</b>");
		}

		return string;
	}

	static
	private String italic(String string, boolean html){

		if(html){
			string = ("<i>" + string + "</i>");
		}

		return string;
	}


	static
	private class BookTitleFormat extends FieldFormat {

		public BookTitleFormat(String separator){
			super(BibTeXEntry.KEY_BOOKTITLE, separator);
		}

		@Override
		public String format(Value value, boolean latex, boolean html){
			String string = super.format(value, latex, html);

			return italic(string, html);
		}
	}

	static
	private class DOIFormat extends FieldFormat {

		public DOIFormat(String separator){
			super(BibTeXEntry.KEY_DOI, separator);
		}

		@Override
		public String format(Value value, boolean latex, boolean html){
			String string = super.format(value, latex, html);

			if(html){
				string = ("DOI: <a href=\"http://dx.doi.org/" + string + "\">" + string + "</a>");
			} else

			{
				string = ("DOI: " + string);
			}

			return string;
		}
	}

	static
	private class EditorFormat extends FieldFormat {

		public EditorFormat(String separator){
			super(BibTeXEntry.KEY_EDITOR, separator);
		}

		@Override
		public String format(Value value, boolean latex, boolean html){
			String string = super.format(value, latex, html);

			string = string.replace(" and ", "; ");

			boolean plural = string.contains("; ");

			string = (string + ", " + (plural ? "Eds." : "Ed."));

			return string;
		}
	}

	static
	private class InBookTitleFormat extends BookTitleFormat {

		public InBookTitleFormat(String separator){
			super(separator);
		}

		@Override
		public String format(Value value, boolean latex, boolean html){
			String string = super.format(value, latex, html);

			string = ("In " + string);

			return string;
		}
	}

	static
	private class JournalFormat extends FieldFormat {

		public JournalFormat(String separator){
			super(BibTeXEntry.KEY_JOURNAL, separator);
		}

		@Override
		public String format(Value value, boolean latex, boolean html){
			String string = super.format(value, latex, html);

			return italic(string, html);
		}
	}

	static
	private class YearFormat extends FieldFormat {

		public YearFormat(String separator){
			super(BibTeXEntry.KEY_YEAR, separator);
		}

		@Override
		public String format(Value value, boolean latex, boolean html){
			String string = super.format(value, latex, html);

			return bold(string, html);
		}
	}

	static
	private class VolumeFormat extends FieldFormat {

		public VolumeFormat(String separator){
			super(BibTeXEntry.KEY_VOLUME, separator);
		}

		@Override
		public String format(Value value, boolean latex, boolean html){
			String string = super.format(value, latex, html);

			return italic(string, html);
		}
	}

	static
	private class URLFormat extends FieldFormat {

		public URLFormat(String separator){
			super(BibTeXEntry.KEY_URL, separator);
		}

		@Override
		public String format(Value value, boolean latex, boolean html){
			String string = super.format(value, latex, html);

			if(html){
				return "<a href=\"" + string + "\">" + string + "</a>";
			} else {
				return string;
			}
		}
	}
}