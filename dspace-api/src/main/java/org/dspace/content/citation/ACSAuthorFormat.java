/*
 * Copyright (c) 2015 University of Tartu
 */
package org.dspace.content.citation;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Value;

public class ACSAuthorFormat extends FieldFormat {

	public ACSAuthorFormat(String separator) {
		super(BibTeXEntry.KEY_AUTHOR, separator);
	}

	@Override
	public String format(Value value, boolean latex, boolean html) {
		String string = super.format(value, latex, false);
		string = normalize(string); // TODO: handle braces 
		string = string.replace(" and ", "; ");
		return string;
	}
	
	public static String normalize(String string){
		StringBuilder sb = new StringBuilder();

		String separator = " and ";
		for (String a: string.split(separator)){
			sb.append(separator);

			String lastName;
			ArrayList<String> names;
			if (a.contains(",")) { // Lastname, Firstname1 ...
				int pos = a.indexOf(',');
				lastName = a.substring(0, pos).trim();
				names = new ArrayList(splitter.splitToList(a.substring(pos+1)));
			} else { // Firstname1 ... Lastname
				names = new ArrayList(splitter.splitToList(a));
				lastName = names.remove(names.size()-1);
			}

			sb.append(lastName);

			if (!names.isEmpty()){
				sb.append(',');
			}
			for (String name: names){
				String initial = name.replaceAll("(?U)(\\w)\\w*", "$1.");
				if (!initial.startsWith("-")) { // avoid space for: "A.-B."
					sb.append(' ');
				}
				sb.append(initial);
			}
		}

		return sb.substring(separator.length());
	}

	private static final Splitter splitter =
			Splitter.on(CharMatcher.anyOf(" .")).trimResults().omitEmptyStrings();
}
