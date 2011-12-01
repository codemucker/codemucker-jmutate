package com.bertvanbrakel.test.finder;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.Collection;

public class IncludeExcludeFileMatcher implements FileMatcher {

	private final Collection<FileMatcher> excludeMatchers = newArrayList();
	private final Collection<FileMatcher> includeMatchers = newArrayList();

	@Override
	public boolean matchFile(File file, String relPath) {
		boolean include = true;
		if (includeMatchers != null && includeMatchers.size() > 0) {
			include = false;// by default if we have includes we exclude
			                // all except matches
			for (FileMatcher matcher : includeMatchers) {
				if (matcher.matchFile(null, relPath)) {
					include = true;
					break;
				}
			}
		}
		if (include && (excludeMatchers != null && excludeMatchers.size() > 0)) {
			for (FileMatcher matcher : excludeMatchers) {
				if (matcher.matchFile(null, relPath)) {
					include = false;
					break;
				}
			}
		}
		return include;
	}

	public IncludeExcludeFileMatcher addInclude(FileMatcher matcher) {
		includeMatchers.add(matcher);
		return this;
	}

	public IncludeExcludeFileMatcher addExclude(FileMatcher matcher) {
		excludeMatchers.add(matcher);
		return this;
	}
}
