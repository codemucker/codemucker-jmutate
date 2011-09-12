package com.bertvanbrakel.test.finder;

import java.io.File;

public class FileWalkerFilter extends FileWalker {

	private FileMatcher includeFileMatcher;
	private FileMatcher excludeFileMatcher;

	public FileWalkerFilter() {
		super();
	}

	@Override
	public boolean isIncludeFile(String relativePath, File file) {
		if (excludeFileMatcher != null && excludeFileMatcher.matchFile(file, relativePath)) {
			return false;
		}
		if (includeFileMatcher != null && !includeFileMatcher.matchFile(file, relativePath)) {
			return false;
		}
		return true;
	}

	public void setIncludes(FileMatcher includeFileMatcher) {
		this.includeFileMatcher = includeFileMatcher;
	}

	public void setExcludes(FileMatcher excludeFileMatcher) {
		this.excludeFileMatcher = excludeFileMatcher;
	}

}
