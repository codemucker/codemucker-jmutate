package com.bertvanbrakel.codemucker.ast.finder;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.bertvanbrakel.test.finder.matcher.Matcher;

/**
 * Iterator which filters a source iterator
 * @param <T>
 */
public class FilteringIterator<T> implements Iterator<T> {

	private final Iterator<T> source;
	private final Matcher<T> matcher;
	
	private T nextItem;

	/**
	 * @param source the backing iterator which provides the item to iterate over
	 * @param matcher the matcher which filters the source iterator
	 */
	public FilteringIterator(Iterator<T> source, Matcher<T> matcher) {
		checkNotNull("types", source);
		checkNotNull("matcher", matcher);
		this.source = source;
		this.matcher = matcher;

		nextItem = nextItem();
	}

	@Override
	public boolean hasNext() {
		return nextItem != null;
	}

	@Override
	public T next() {
		if (nextItem == null) {
			throw new NoSuchElementException();
		}
		T ret = nextItem;
		nextItem = nextItem();
		return ret;
	}

	private T nextItem() {
		while (source.hasNext()) {
			T type = source.next();
			if (matcher.matches(type)) {
				return type;
			}
		}
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}