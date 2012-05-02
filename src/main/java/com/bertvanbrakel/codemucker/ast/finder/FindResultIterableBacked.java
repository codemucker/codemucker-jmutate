package com.bertvanbrakel.codemucker.ast.finder;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bertvanbrakel.test.finder.matcher.Matcher;


public class FindResultIterableBacked<T> implements FindResult<T> {

	private final Iterable<T> source;
	private Boolean empty;

	public static <T> FindResultIterableBacked<T> from(Iterable<T> source){
		return new FindResultIterableBacked<T>(source);
	}
	
	public static <T> FindResultIterableBacked<T> from(Iterator<T> results){
		return new FindResultIterableBacked<T>(toIterable(results));
	}
	
	@Override
	public boolean isEmpty(){
		if( empty == null){
			if( source instanceof Collection){
				empty = ((Collection<?>)source).isEmpty();
			} else {
				empty = source.iterator().hasNext();
			}
		}
		return empty.booleanValue();
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
    public FindResultIterableBacked(Iterable<T> results) {
	    super();
	    this.source = results==null?Collections.EMPTY_LIST:results;
    }

	@Override
    public Iterator<T> iterator() {
	    return source.iterator();
    }

	@Override
    public List<T> toList() {
		if( source instanceof List){
			return (List<T>)source;
		} else {
			return newArrayList(source);
		}
	}

	@Override
    public FindResult<T> filter(Matcher<T> matcher) {
		return FindResultIterableBacked.from(new FilteringIterator<T>(source.iterator(), matcher));
    }

	@Override
    public <K> Map<K, T> toMap(KeyProvider<K, T> maker) {
		Map<K, T> map = newHashMap();
		for(T item:this){
			K key = maker.getKeyFor(item);
			map.put(key, item);
		}
	    return map;
    }

	@Override
    public T getFirst() {
	    return iterator().next();
    }
}
