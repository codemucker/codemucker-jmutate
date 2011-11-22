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
package com.bertvanbrakel.test.bean.tester;

import com.bertvanbrakel.test.bean.random.RandomOptions;

public class HashCodeEqualsOptions extends RandomOptions {
	private boolean testCtors = true;
	private boolean testCtorsModifyEquals = true;
	private boolean testProperties = true;
	private boolean testCtorsArgsMatchProperties = true;
	
	public boolean isTestProperties() {
		return testProperties;
	}

	public HashCodeEqualsOptions testProperties(boolean testProperties) {
		this.testProperties = testProperties;
		return this;
	}

	public boolean isTestCtors() {
		return testCtors;
	}

	public HashCodeEqualsOptions testCtors(boolean testCtors) {
		this.testCtors = testCtors;
		return this;
	}

	public boolean isTestCtorsModifyEquals() {
		return testCtorsModifyEquals;
	}

	public HashCodeEqualsOptions testCtorsModifyEquals(boolean testCtorsModifyEquals) {
		this.testCtorsModifyEquals = testCtorsModifyEquals;
		return this;
	}

	public boolean isTestCtorsArgsMatchProperties() {
		return testCtorsArgsMatchProperties;
	}

	public HashCodeEqualsOptions testCtorsArgsMatchProperties(boolean testCtorsArgsMatchProperties) {
		this.testCtorsArgsMatchProperties = testCtorsArgsMatchProperties;
		return this;
	}

}
