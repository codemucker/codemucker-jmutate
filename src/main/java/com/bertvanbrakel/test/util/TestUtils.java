/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bertvanbrakel.test.util;

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
