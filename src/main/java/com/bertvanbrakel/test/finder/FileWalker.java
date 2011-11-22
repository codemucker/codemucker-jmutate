/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bertvanbrakel.test.finder;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

public class FileWalker {
	
	private static FileFilter DIR_FILTER = new FileFilter() {
		private static final char HIDDEN_DIR_PREFIX = '.';//like .git, .svn,....
		
		@Override
		public boolean accept(File dir) {
			return dir.isDirectory() && dir.getName().charAt(0) != HIDDEN_DIR_PREFIX && !dir.getName().equals("CVS");
		}
	};
	
	private static FileFilter FILE_FILTER = new FileFilter() {
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