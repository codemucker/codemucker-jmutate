package com.bertvanbrakel.codemucker.ast.finder.matcher;

import java.io.File;
import java.util.regex.Pattern;

import com.bertvanbrakel.test.finder.FileMatcher;

public class RegExpPatternFileNameMatcher implements FileMatcher {
	private final Pattern pattern;

	public RegExpPatternFileNameMatcher(String pattern) {
		this(Pattern.compile(pattern));
	}

	public RegExpPatternFileNameMatcher(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean matchFile(File file, String path) {
		return pattern.matcher(path).matches();
	}
}