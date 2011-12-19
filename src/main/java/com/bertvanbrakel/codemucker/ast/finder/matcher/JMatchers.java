package com.bertvanbrakel.codemucker.ast.finder.matcher;

import static com.google.common.base.Preconditions.checkArgument;


public class JMatchers {

	/**
     * Synonym for {@link #and(JTypeMatcher...)}
     */
    public static <T> Matcher<T> all(final Matcher<T>... matchers) {
    	return and(matchers);
    }

	public static <T> Matcher<T> and(final Matcher<T>... matchers) {
    	return new Matcher<T>() {
    		@Override
    		public boolean matches(T found) {
    			for(Matcher<T> matcher:matchers){
    				if( !matcher.matches(found)){
    					return false;
    				}
    			}
    			return true;
    		}
    	};
    }

	/**
     * Synonym for {@link #or(JTypeMatcher...)}
     */
    public static <T> Matcher<T> either(final Matcher<T>... matchers) {
    	return or(matchers);
    }

	/**
     * Synonym for {@link #or(JTypeMatcher...)}
     */
    public static <T> Matcher<T> any(final Matcher<T>... matchers) {
    	return or(matchers);
    }

	public static <T> Matcher<T> or(final Matcher<T>... matchers) {
    	return new Matcher<T>() {
    		@Override
    		public boolean matches(T found) {
    			for(Matcher<T> matcher:matchers){
    				if( matcher.matches(found)){
    					return true;
    				}
    			}
    			return false;
    		}
    	};
    }

	public static <T> Matcher<T> not(final Matcher<T> matcher) {
    	return new Matcher<T>() {
    		@Override
    		public boolean matches(T found) {
    			return !matcher.matches(found);
    		}
    	};
    }

	public static Matcher<Integer> equalTo(final int require) {
		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() == require;
			}
		};
	}

	public static Matcher<Integer> greaterThan(final int require) {
		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() > require;
			}
		};
	}

	public static Matcher<Integer> greaterOrEqualTo(final int require) {
		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() >= require;
			}
		};
	}

	public static Matcher<Integer> lessThan(final int require) {
		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() > require;
			}
		};
	}

	public static Matcher<Integer> lessOrEqualTo(final int require) {
		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() <= require;
			}
		};
	}

	public static Matcher<Integer> inRange(final int from, final int to) {
		checkArgument(from >= 0, "Expect 'from' to be >= 0");
		checkArgument(to >= 0, "Expect 'to' to be >= 0");
		checkArgument(from <= to, "Expect 'from' to be <= 'to'");

		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				int val = found.intValue();
				return val >= from && val <= to;
			}
		};
	}

}
