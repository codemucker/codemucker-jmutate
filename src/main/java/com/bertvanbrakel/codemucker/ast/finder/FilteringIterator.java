package com.bertvanbrakel.codemucker.ast.finder;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.bertvanbrakel.codemucker.ast.finder.matcher.Matcher;

class FilteringIterator<T> implements Iterator<T> {

	private final Iterator<T> items;
	private final Matcher<T> matcher;
	
	private T nextItem;

	public FilteringIterator(Iterator<T> types, Matcher<T> matcher) {
		checkNotNull("types", types);
		checkNotNull("matcher", matcher);
		this.items = types;
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
		while (items.hasNext()) {
			T type = items.next();
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