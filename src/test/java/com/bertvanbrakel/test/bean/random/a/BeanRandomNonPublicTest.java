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
