package com.bertvanbrakel.codemucker.ast.finder;

import java.util.List;
import java.util.Map;

import com.bertvanbrakel.test.finder.matcher.Matcher;


public interface FindResult<T> extends Iterable<T> {

	public List<T> asList();

	/**
	 * Return a new view over the current results using the given filter
	 * @param matcher used to filter the original results
	 * @return
	 */
	public FindResult<T> filter(Matcher<T> matcher);
	
	/**
	 * Return the results as a map using the given key provider to generate keys
	 * @param keyProvider
	 * @return
	 */
	public <K> Map<K,T> asMap(KeyProvider<K, T> keyProvider);

	public static interface KeyProvider<K,V> {
		public K getKeyFor(V value);
	}
}
