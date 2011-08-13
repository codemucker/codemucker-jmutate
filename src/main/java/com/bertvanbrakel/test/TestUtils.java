package com.bertvanbrakel.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TestUtils {

	public static <T extends Comparable<T>> List<T> sorted(T... items) {
		List<T> list = list(items);
		Collections.sort(list);
		return list;
	}

	public static <T extends Comparable<T>> List<T> sorted(Comparator<T> comparator, T... items) {
		List<T> list = list(items);
		Collections.sort(list, comparator);
		return list;
	}

	public static <T extends Comparable<T>> List<T> sorted(Iterable<T> items) {
		List<T> list = list(items);
		Collections.sort(list);
		return list;
	}

	public static <T extends Comparable<T>> List<T> sorted(Comparator<T> comparator, Iterable<T> items) {
		List<T> list = list(items);
		Collections.sort(list, comparator);
		return list;
	}
	
	public static <T> List<T> list(Iterable<T> items) {
		List<T> list = list();
		for (T item : items) {
			list.add(item);
		}
		return list;
	}

	public static <T> List<T> list(T... items) {
		return Arrays.asList(items);
	}

	public static <T> List<T> list() {
		return new ArrayList<T>();
	}

}
