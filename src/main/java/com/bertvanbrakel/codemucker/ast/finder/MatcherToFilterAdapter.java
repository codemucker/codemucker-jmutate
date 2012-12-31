package com.bertvanbrakel.codemucker.ast.finder;

import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.google.common.base.Preconditions;

class MatcherToFilterAdapter<T> implements FindResult.Filter<T> {
	private final Matcher<T> matcher;

	public static <T> FindResult.Filter<T> from(Matcher<T> matcher){
		return new MatcherToFilterAdapter<T>(matcher);
	}
	
	private MatcherToFilterAdapter(Matcher<T> matcher){
		Preconditions.checkNotNull(matcher, "expect non null matcher");
		this.matcher = matcher;
	}
	
	@Override
	public boolean matches(T found) {
		return matcher.matches(found);
	}

	@Override
	public void onMatched(T result) {
		//do nothing
	}

	@Override
	public void onIgnored(T result) {
		//do nothing
	}
}