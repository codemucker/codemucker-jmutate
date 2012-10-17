package com.bertvanbrakel.codemucker.ast.matcher;

import java.util.regex.Pattern;

import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.test.finder.matcher.Matcher;

public class RegExpPatternTypeNameMatcher implements Matcher<JType> {
	private final Pattern pattern;

	public RegExpPatternTypeNameMatcher(String pattern) {
		this(Pattern.compile(pattern));
	}

	public RegExpPatternTypeNameMatcher(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean matches(JType type) {
		return pattern.matcher(type.getSimpleName()).matches();
	}
}