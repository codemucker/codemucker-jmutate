package com.bertvanbrakel.test.finder;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

class FileWalker {
	FileFilter DIR_FILTER = new FileFilter() {
		@Override
		public boolean accept(File f) {
			return f.isDirectory() && f.getName().charAt(0) != '.';
		}
	};
	FileFilter FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File f) {
			return f.isFile();
		}
	};

	public final Collection<String> findFiles(File dir) {
		Collection<String> foundFiles = new ArrayList<String>();
		walkDir("", dir, 0, foundFiles);
		return foundFiles;
	}

	private void walkDir(String parentPath, File dir, int depth, Collection<String> foundFiles) {
		File[] files = dir.listFiles(FILE_FILTER);
		for (File f : files) {
			String path = parentPath + "/" + f.getName();
			if (isIncludeFile(path, f)) {
				foundFiles.add(path);
			}
		}
		File[] childDirs = dir.listFiles(DIR_FILTER);
		for (File childDir : childDirs) {
			if (isWalkDir(childDir)) {
				walkDir(parentPath + "/" + childDir.getName(), childDir, depth + 1, foundFiles);
			}
		}
	}

	public boolean isWalkDir(File dir) {
		return true;
	}

	public boolean isIncludeFile(String relativePath, File file) {
		return true;
	}
}