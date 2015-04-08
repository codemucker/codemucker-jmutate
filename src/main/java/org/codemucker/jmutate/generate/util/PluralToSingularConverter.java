package org.codemucker.jmutate.generate.util;

import java.util.HashMap;
import java.util.Map;

public class PluralToSingularConverter {

	public static final PluralToSingularConverter INSTANCE = new PluralToSingularConverter();

	private Map<String, String> pluralToSingular = new HashMap<>();
	private Map<String, String> singleToPlural = new HashMap<>();

	private PluralToSingularConverter() {
		addSingleToPlural("person", "people");
		addSingleToPlural("fish", "fish");
		addSingleToPlural("sheep", "sheep");
		addSingleToPlural("cow", "cows");
		// addSingleToPlural("company","companies");
		// addSingleToPlural("address","adresses");
		// addSingleToPlural("city","cities");
		addSingleToPlural("series", "series");
		addSingleToPlural("child", "children");
		addSingleToPlural("money", "monies");
		addSingleToPlural("man", "men");
		addSingleToPlural("woman", "women");
		addSingleToPlural("use", "uses");
		addSingleToPlural("leaf", "leaves");

	}

	private void addSingleToPlural(String single, String plural) {
		pluralToSingular.put(plural, single);
		singleToPlural.put(single, plural);
	}

	public String singleToPlural(String single) {
		if (single == null) {
			return null;
		}
		String plural = this.singleToPlural.get(single);
		if (plural == null) {
			if (pluralToSingular.containsKey(single)) {
				return plural;
			}
			if (single.endsWith("y")) {
				String w = removeLastChar(single);
				if (endsWithVowel(w)) {
					plural = w + "ys";
				} else {
					plural = w + "ies";
				}
			} else if (isEsEnding(single)) {
				plural = single + "es";
			} else if (single.endsWith("f")) {
				plural = removeLastChar(single) + "ves";
			} else if (single.endsWith("fe")) {
				plural = removeLastChars(single, 2) + "ves";
			} else {
				plural = single + "s";
			}
		}
		return plural;
	}

	public String pluralToSingle(String plural) {
		if (plural == null) {
			return null;
		}
		String singular = this.pluralToSingular.get(plural);
		if (singular == null) {
			if (singleToPlural.containsKey(plural)) {
				return plural;
			}
			if (plural.endsWith("ies")) {
				singular = removeLastChars(plural, 3) + "y";
			} else if (plural.endsWith("ves")) {
				String w = removeLastChars(plural, 3);
				if (endsWithVowel(w)) {
					singular = w + "fe";
				} else {
					singular = w + "f";
				}
			} else if (plural.endsWith("ys")) {
				singular = removeLastChar(plural);
			} else if (plural.endsWith("es")) {
				String w = removeLastChars(plural, 2);
				if (isEsEnding(w)) {
					singular = w;
				} else {
					singular = removeLastChar(plural);
				}
			} else if (plural.endsWith("s")) {
				singular = removeLastChar(plural);
			} else {
				singular = plural;
			}
		}
		return singular;
	}

	private static boolean isEsEnding(String w) {
		return w.endsWith("ss") || w.endsWith("ch") || w.endsWith("ex")
				|| w.endsWith("is") || w.endsWith("ix") || w.endsWith("nx")
				|| w.endsWith("us");
	}

	private static String removeLastChar(String s) {
		return removeLastChars(s, 1);
	}

	private static String removeLastChars(String s, int num) {
		return s.substring(0, s.length() - num);
	}

	private static boolean endsWithVowel(String s) {
		return isVowel(s.charAt(s.length() - 1));
	}

	private static boolean isVowelFromEnd(String s, int pos) {
		return isVowel(s.charAt(s.length() - pos - 1));
	}

	private static boolean isVowel(String s, int pos) {
		return isVowel(s.charAt(pos));
	}

	private static boolean isVowel(char c) {
		c = Character.toLowerCase(c);
		switch (c) {
		case 'a':
		case 'e':
		case 'i':
		case 'o':
		case 'u':
			return true;
		default:
			return false;
		}
	}
}