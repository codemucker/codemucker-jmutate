package com.bertvanbrakel.test.bean.random;

import static com.bertvanbrakel.test.TestUtils.sorted;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Test;

import com.bertvanbrakel.test.bean.BeanException;
import com.bertvanbrakel.test.bean.TstBeanIgnoreProperty;

public class BeanRandomTest {

	@Test
	public void test_no_arg_ctor() {
		TstBeanNoArgCtor bean = new BeanRandom().populate(TstBeanNoArgCtor.class);
		assertNotNull(bean);
	}

	@Test
	public void test_private_no_arg_ctor() {
		TstBeanPrivateNoArgCtor bean = new BeanRandom().populate(TstBeanPrivateNoArgCtor.class);
		assertNotNull(bean);
	}

	@Test
	public void test_multi_arg_ctor() {
		TstBeanMultiArgCtor bean = new BeanRandom().populate(TstBeanMultiArgCtor.class);
		assertNotNull(bean);
		assertNotNull(bean.getFieldA());
		assertNotNull(bean.getFieldB());
	}

	@Test
	public void test_fields_populated_via_setters() {
		TstBeanSetters bean = new BeanRandom().populate(TstBeanSetters.class);
		assertNotNull(bean);

		String[] expectFieldsSet = { "boolean", "byte", "char", "short", "int", "long", "double", "float", "String",
		        "Boolean", "Byte", "Character", "Short", "Integer", "Long", "Double", "Float", "BigDecimal",
		        "BigInteger" };

		assertEquals(sorted(expectFieldsSet), sorted(bean.fieldToValues.keySet()));

		for (Entry<String, Object> entry : bean.fieldToValues.entrySet()) {
			assertNotNull("expected vale for field " + entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void test_array_property() {
		TstBeanArray bean = new BeanRandom().populate(TstBeanArray.class);
		assertNotNull(bean);
		assertArrayIsPopulated(bean.getStringArray());
		assertArrayIsPopulated(bean.getFloatArray());
		assertArrayIsPopulated(bean.getIntegerArray());
		assertArrayIsPopulated(bean.getCharArray());
		assertArrayIsPopulated(bean.getDoubleArray());
		assertArrayIsPopulated(bean.getBigDecimalArray());
		assertArrayIsPopulated(bean.getBigIntegerArray());
		assertArrayIsPopulated(bean.getBooleanArray());

		assertArrayIsPopulated(bean.getBeanArray());
	}

	@Test
	public void test_enum_property() {
		TstBeanEnum bean = new BeanRandom().populate(TstBeanEnum.class);
		assertNotNull(bean);
		assertNotNull(bean.getEnumField());
	}

	@Test
	public void test_infinite_recursion_passes() {
		// TODO:set option = no fail
		BeanRandom tester = new BeanRandom();
		tester.getOptions().failOnRecursiveBeanCreation(false);

		TstBeanSelf bean = tester.populate(TstBeanSelf.class);
		assertNotNull(bean);
		assertNotNull(bean.getFieldSelf());
		assertNull(bean.getFieldSelf().getFieldSelf());
	}

	@Test
	public void test_complex_property() {
		TstBeanComplexProperty bean = new BeanRandom().populate(TstBeanComplexProperty.class);
		assertNotNull(bean);
		assertNotNull(bean.getFieldComplex());
		assertNotNull(bean.getFieldComplex().getFieldA());
		assertNotNull(bean.getFieldComplex().getFieldB());
	}

	@Test
	public void test_ignore_property() {
		BeanRandom tester = new BeanRandom();
		tester.getOptions().ignoreProperty("fieldB").failOnRecursiveBeanCreation(false);

		TstBeanIgnoreProperty bean = tester.populate(TstBeanIgnoreProperty.class);
		assertNotNull(bean);
		assertNotNull(bean.getFieldA());
		assertNull(bean.getFieldB());
		assertNotNull(bean.getFieldC());
	}

	@Test
	public void test_ignore_deep_property() {
		BeanRandom tester = new BeanRandom();
		tester.getOptions().ignoreProperty("fieldC.fieldB")
		// .ignoreProperty(TstBeanIgnoreProperty.class, "fieldA")
		// .ignoreProperty("*A")

		        .failOnRecursiveBeanCreation(false);

		TstBeanIgnoreProperty bean = tester.populate(TstBeanIgnoreProperty.class);
		assertNotNull(bean);
		assertNotNull(bean.getFieldA());
		assertNotNull(bean.getFieldB());
		assertNotNull(bean.getFieldC());
		assertNotNull(bean.getFieldC().getFieldA());
		assertNull(bean.getFieldC().getFieldB());
		assertNull(bean.getFieldC().getFieldC());
	}

	@Test
	public void test_ignore_field_on_bean_type() {
		BeanRandom tester = new BeanRandom();
		tester.getOptions().ignoreProperty(TstBeanIgnoreProperty.class, "fieldA")
		        .ignoreProperty(TstBeanIgnoreProperty.class, "fieldC");

		TstBeanIgnoreBeanPropertyType bean = tester.populate(TstBeanIgnoreBeanPropertyType.class);
		assertNotNull(bean);
		assertNotNull(bean.getFieldA());
		assertNotNull(bean.getFieldB());
		assertNotNull(bean.getFieldB().getFieldB());

		assertNull(bean.getFieldB().getFieldA());
		assertNull(bean.getFieldB().getFieldC());
	}

	@Test
	public void test_infinite_recursion_fails() {
		try {
			TstBeanSelf bean = new BeanRandom().populate(TstBeanSelf.class);
			Assert.fail("Expected exception");
		} catch (BeanException e) {
			assertMsgContainsAll(e, "fieldSelf", "recursive", TstBeanSelf.class.getName());
		}
	}

	private void assertMsgContainsAll(Throwable t, String... text) {
		assertNotNull("Expected error message", t.getMessage());
		String msg = t.getMessage().toLowerCase();
		for (String s : text) {
			s = s.toLowerCase();
			if (!msg.contains(s)) {
				fail(String.format("Expected message '%s' to contain '%s'", t.getMessage(), s));
			}
		}
	}

	private <T> void assertArrayIsPopulated(T[] arr) {
		assertNotNull(arr);
		assertTrue("expected atleast one item in the array", arr.length > 0);
		for (T ele : arr) {
			assertNotNull("array element is null", ele);
		}
	}

}
