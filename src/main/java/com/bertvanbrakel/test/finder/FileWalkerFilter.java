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
