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
package com.bertvanbrakel.test.bean.random.a;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.bertvanbrakel.test.bean.BeanException;
import com.bertvanbrakel.test.bean.random.BeanRandom;

public class BeanRandomNonPublicTest {

	@Test
	public void test_non_public_beans_handled() {
		boolean error = false;
		try {
			new BeanRandom().populate(TstBeanNonPublic.class);
		} catch (BeanException e) {
			error = true;
		}
		assertTrue("Expected error", error);

		BeanRandom random = new BeanRandom();
		random.getOptions().makeAccessible(true);
		TstBeanNonPublic bean = random.populate(TstBeanNonPublic.class);
		assertNotNull(bean);
		assertNotNull(bean.getMyField());
	}
}
