package com.bertvanbrakel.codemucker.matcher;

import static com.google.common.base.Preconditions.checkArgument;

import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.google.common.base.Objects;


public class AInt extends LogicalMatchers {

	public static Matcher<Integer> equalTo(final int require) {
		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() == require;
			}
			
			@Override
    		public String toString(){
    			return Objects.toStringHelper("MatcheEqualTo").add("val", require).toString();
    		}
		};
	}

	public static Matcher<Integer> greaterThan(final int require) {
		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() > require;
			}
			
			@Override
    		public String toString(){
    			return Objects.toStringHelper("MatchGreaterThan").add("val", require).toString();
    		}
		};
	}

	public static Matcher<Integer> greaterOrEqualTo(final int require) {
		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() >= require;
			}
			@Override
    		public String toString(){
    			return Objects.toStringHelper("MatchGreaterOrEqualTo").add("val", require).toString();
    		}
		};
	}

	public static Matcher<Integer> lessThan(final int require) {
		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() > require;
			}
			
			@Override
    		public String toString(){
    			return Objects.toStringHelper("MatchLessThan").add("val", require).toString();
    		}
		};
	}

	public static Matcher<Integer> lessOrEqualTo(final int require) {
		return new Matcher<Integer>() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() <= require;
			}
			
			@Override
    		public String toString(){
    			return Objects.toStringHelper("MatchLessThanOrEqualTo").add("val", require).toString();
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
			
			@Override
    		public String toString(){
    			return Objects.toStringHelper("MatchRangeInclusive").add("from", from).add("to", to).toString();
    		}
		};
	}
}
