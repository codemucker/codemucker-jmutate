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

import static junit.framework.Assert.assertTrue;
import junit.framework.AssertionFailedError;

import org.junit.Test;

import com.bertvanbrakel.test.bean.tester.hashcodeequals.TstBeanBrokenCtor;
import com.bertvanbrakel.test.bean.tester.hashcodeequals.TstBeanHashCodeChangesPerInvocation;
import com.bertvanbrakel.test.bean.tester.hashcodeequals.TstBeanNonEqualHashcode;
import com.bertvanbrakel.test.bean.tester.hashcodeequals.TstBeanOk;
import com.bertvanbrakel.test.bean.tester.hashcodeequals.TstBeanPropertyNotIncludedInEquals;

public class HashCodeEqualsTesterTest {

	@Test
	public void test_ok_bean() {
		new HashCodeEqualsTester().checkHashCodeEquals(TstBeanOk.class);
	}

	@Test
	public void test_broken_ctor_properties_ok() {
		HashCodeEqualsTester tester = new HashCodeEqualsTester();
		tester.getOptions().testCtorsArgsMatchProperties(false);

		tester.getOptions().testCtorsModifyEquals(false);
		tester.checkHashCodeEquals(TstBeanBrokenCtor.class);
	}

	@Test
	public void test_fails_on_broken_ctor() {
		HashCodeEqualsTester tester = new HashCodeEqualsTester();
		tester.getOptions().testCtors(true).testCtorsModifyEquals(true);

		assertFailed("Expected failure when ctor args doe not modify property with same name", tester, TstBeanBrokenCtor.class);
	}

	@Test
	public void test_property_not_included_in_equals_fails() {
		HashCodeEqualsTester tester = new HashCodeEqualsTester();
		tester.getOptions().testCtors(true).testCtorsModifyEquals(true);

		assertFailed("Expected failure when property not included in equals", tester, TstBeanPropertyNotIncludedInEquals.class);
	}

	@Test
	public void test_property_not_included_in_equals_can_be_ignored() {
		HashCodeEqualsTester tester = new HashCodeEqualsTester();
		tester.getOptions().testCtors(true).testCtorsModifyEquals(true).ignoreProperty(TstBeanPropertyNotIncludedInEquals.class, "fieldIgnore");

		tester.checkHashCodeEquals(TstBeanPropertyNotIncludedInEquals.class);
	}
	
	@Test
	public void test_hashcode_changes_per_invocation_fails() {
		HashCodeEqualsTester tester = new HashCodeEqualsTester();
		assertFailed("Expected failure on hashcode which changes per invocation", tester, TstBeanHashCodeChangesPerInvocation.class);
	}
	
	@Test
	public void test_equal_beans_with_non_equal_hashcodes_fail() {
		HashCodeEqualsTester tester = new HashCodeEqualsTester();
		assertFailed("Expected failure on hashcode which doe not equal on equal beans", tester, TstBeanNonEqualHashcode.class);
	}
	
	
	private void assertFailed(String msg, HashCodeEqualsTester tester, Class<?> klass) {
		boolean failed = false;
		try {
			tester.checkHashCodeEquals(TstBeanBrokenCtor.class);
		} catch (AssertionFailedError e) {
			failed = true;
		}
		assertTrue(msg, failed);
	}
}
