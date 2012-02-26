package com.bertvanbrakel.codemucker.ast.finder;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bertvanbrakel.codemucker.ast.finder.matcher.Matcher;

public class FindResultImpl<T> implements FindResult<T> {

	private final Iterable<T> results;

	public static <T> FindResultImpl<T> from(Iterable<T> results){
		return new FindResultImpl<T>(results);
	}
	
	public static <T> FindResultImpl<T> from(Iterator<T> results){
		return new FindResultImpl<T>(toIterable(results));
	}
	
	private static <T> Iterable<T> toIterable(Iterator<T> iter){
		List<T> list = newArrayList();
		while( iter.hasNext()){
			T ele = iter.next();
			list.add(ele);
		}
		return list;
	}
	
	/**
	 * @param results can be null in which case it is treated as an empty list
	 */
	@SuppressWarnings("unchecked")
    public FindResultImpl(Iterable<T> results) {
	    super();
	    this.results = results==null?Collections.EMPTY_LIST:results;
    }

	@Override
    public Iterator<T> iterator() {
	    return results.iterator();
    }

	@Override
    public List<T> asList() {
		if( results instanceof List){
			return (List<T>)results;
		} else {
			return newArrayList(results);
		}
	}

	@Override
    public FindResult<T> filter(Matcher<T> matcher) {
		return FindResultImpl.from(new FilteringIterator<T>(results.iterator(), matcher));
    }

	@Override
    public <K> Map<K, T> asMap(KeyProvider<K, T> maker) {
		Map<K, T> map = newHashMap();
		for(T item:this){
			K key = maker.getKeyFor(item);
			map.put(key, item);
		}
	    return map;
    }
}
