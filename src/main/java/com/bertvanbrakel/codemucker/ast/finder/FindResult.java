package com.bertvanbrakel.codemucker.ast.finder;

import java.util.List;
import java.util.Map;

import com.bertvanbrakel.codemucker.ast.finder.matcher.Matcher;

public interface FindResult<T> extends Iterable<T> {

	public List<T> asList();
	
	public FindResult<T> filter(Matcher<T> matcher);
	
	public <K> Map<K,T> asMap(KeyProvider<K, T> keyProvider);

	public static interface KeyProvider<K,V> {
		public K getKeyFor(V value);
	}
}
