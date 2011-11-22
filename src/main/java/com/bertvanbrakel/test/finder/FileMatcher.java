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