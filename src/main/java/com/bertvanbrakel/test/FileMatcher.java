package com.bertvanbrakel.test;

import java.io.File;

public interface FileMatcher {
	/**
	 * Does the given file match?
	 * 
	 * @param file
	 *            the file
	 * @param relPath
	 *            the path relative to the search root
	 * @return true if the file matches
	 */
	public boolean matchFile(File file, String relPath);
}