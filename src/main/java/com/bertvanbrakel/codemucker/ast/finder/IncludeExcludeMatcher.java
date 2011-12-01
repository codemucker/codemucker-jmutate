package com.bertvanbrakel.codemucker.ast.finder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.Collection;

import com.bertvanbrakel.codemucker.ast.finder.matcher.Matcher;

public class IncludeExcludeMatcher<T> implements Matcher<T>{
	//TODO:options to set includeAll or includeAny?
	private final Collection<Matcher<T>> includeMatchers = newArrayList();
	private final Collection<Matcher<T>> excludeMatchers = newArrayList();
	
	private final IncludeMode mode;
	
	public static enum IncludeMode {
		/** If any of the include matches matchers, it's a match*/
		ANY,
		/** All include matchers must match for a match */
		ALL;
	}
	
	public IncludeExcludeMatcher() {
		this(IncludeMode.ANY);
	}

	public IncludeExcludeMatcher(IncludeMode mode) {
		checkNotNull(mode, "expect include mode");
		this.mode = mode;
	}
	
	public boolean matches(T found) {
		boolean include = true;
		if (includeMatchers != null && includeMatchers.size() > 0) {
			if( mode == IncludeMode.ANY ){
				include = false;
				for (Matcher<T> matcher : includeMatchers) {
    				if (matcher.matches(found)) {
    					include = true;
    					break;
    				}
    			}	
			} else {
				include = true;
				for (Matcher<T> matcher : includeMatchers) {
					if (!matcher.matches(found)) {
						include = false;
						break;
					}
				}
			}
		}
		if (excludeMatchers != null && excludeMatchers.size() > 0) {
			for (Matcher<T> matcher : excludeMatchers) {
				if (matcher.matches(found)) {
					include = false;
					break;
				}
			}
		}
		return include;
	}
	
	public void addInclude(Matcher<T> matcher) {
		this.includeMatchers.add(matcher);
	}

	public void addIncludeAll(Matcher<T>... matchers) {
		this.includeMatchers.addAll(Arrays.asList(matchers));
	}
	
	public void addExclude(Matcher<T> matcher) {
		this.excludeMatchers.add(matcher);
	}
	
	public void addExcludeAll(Matcher<T>... matchers) {
		this.excludeMatchers.addAll(Arrays.asList(matchers));
	}
	
}