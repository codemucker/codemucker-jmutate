package com.bertvanbrakel.test.bean;

import static com.bertvanbrakel.test.TestUtils.sorted;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Test;


public class BeanTesterTest {

    @Test
    public void test_no_arg_ctor() {
	TstBeanNoArgCtor bean = new BeanTester().populate(TstBeanNoArgCtor.class);
	assertNotNull(bean);
    }

    @Test
    public void test_private_no_arg_ctor() {
	TstBeanPrivateNoArgCtor bean = new BeanTester().populate(TstBeanPrivateNoArgCtor.class);
	assertNotNull(bean);
    }

    @Test
    public void test_multi_arg_ctor() {
	TstBeanMultiArgCtor bean = new BeanTester().populate(TstBeanMultiArgCtor.class);
	assertNotNull(bean);
	assertNotNull(bean.getFieldA());
	assertNotNull(bean.getFieldB());
    }

    @Test
    public void test_fields_populated_via_setters() {
	TstBeanSetters bean = new BeanTester().populate(TstBeanSetters.class);
	assertNotNull(bean);

	String[] expectFieldsSet = { "boolean", "byte","char", "short", "int",
	        "long", "double", "float", "String", "Boolean", "Byte", "Character",
	        "Short", "Integer", "Long", "Double", "Float", "BigDecimal", "BigInteger" };

	assertEquals(sorted(expectFieldsSet), sorted(bean.fieldToValues.keySet()));

	for (Entry<String, Object> entry : bean.fieldToValues.entrySet()) {
	    assertNotNull("expected vale for field " + entry.getKey(), entry.getValue());
	}
    }

    @Test
    public void test_array_property() {
	TestBeanArray bean = new BeanTester().populate(TestBeanArray.class);
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
	TstBeanEnum bean = new BeanTester().populate(TstBeanEnum.class);
	assertNotNull(bean);
	assertNotNull(bean.getEnumField());
    }

    @Test
    public void test_infinite_recursion_passes(){
	//TODO:set option = no fail
	BeanTester tester = new BeanTester();
	tester.getOptions().failOnRecursiveBeanCreation(false);
	
	TstBeanSelf bean = tester.populate(TstBeanSelf.class);
	assertNotNull(bean);
	assertNotNull(bean.getFieldSelf());
	assertNull(bean.getFieldSelf().getFieldSelf());	
    }
   
    @Test
    public void test_complex_property(){
	TstBeanComplexProperty bean = new BeanTester().populate(TstBeanComplexProperty.class);
	assertNotNull(bean);
	assertNotNull(bean.getFieldComplex());
	assertNotNull(bean.getFieldComplex().getFieldA());
	assertNotNull(bean.getFieldComplex().getFieldB());	
    }

    @Test
    public void test_ignore_property(){
	BeanTester tester = new BeanTester();
	tester.getOptions()
		.ignoreProperty("fieldB")
		.failOnRecursiveBeanCreation(false);
	
	TstBeanIgnoreProperty bean = tester.populate(TstBeanIgnoreProperty.class);
	assertNotNull(bean);
	assertNotNull(bean.getFieldA());
	assertNull(bean.getFieldB());
	assertNotNull(bean.getFieldC());	
    }
    
    @Test
    public void test_ignore_deep_property(){
	BeanTester tester = new BeanTester();
	tester.getOptions()
		.ignoreProperty("fieldC.fieldB")
	//	.ignoreProperty(TstBeanIgnoreProperty.class, "fieldA")
	//	.ignoreProperty("*A")
		
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
    public void test_get_properties_ignore(){
	BeanTester tester = new BeanTester();
	tester.getOptions()
		.ignoreProperty(TstBeanIgnoreProperty.class, "fieldA")
		.ignoreProperty(TstBeanIgnoreProperty.class, "fieldC");

	Map<String, Property> properties = tester.extractProperties(TstBeanIgnoreProperty.class);
	
	assertNotNull(properties);
	Property p = properties.get("fieldC");
	assertNotNull(p);
	assertTrue(p.isIgnore());
    }
    
    @Test
    public void test_ignore_field_on_bean_type(){
	BeanTester tester = new BeanTester();
	tester.getOptions()
		.ignoreProperty(TstBeanIgnoreProperty.class, "fieldA")
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
    public void test_infinite_recursion_fails(){
	try {
	    TstBeanSelf bean = new BeanTester().populate(TstBeanSelf.class);
	    Assert.fail("Expected exception");
	} catch(BeanException e){
	    assertMsgContainsAll(e, "fieldSelf", "recursive", TstBeanSelf.class.getName());
	}
    }
    
    private void assertMsgContainsAll(Throwable t,String...text){
	assertNotNull("Expected error message", t.getMessage());
	String msg = t.getMessage().toLowerCase();
	for( String s:text){
	    s = s.toLowerCase();
	    if( !msg.contains(s)){
		fail(String.format("Expected message '%s' to contain '%s'",t.getMessage(),s));
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
    
    static class TstBeanIgnoreBeanPropertyType {
	private String fieldA;
	private TstBeanIgnoreProperty fieldB;
	public String getFieldA() {
            return fieldA;
        }
	public void setFieldA(String fieldA) {
            this.fieldA = fieldA;
        }
	public TstBeanIgnoreProperty getFieldB() {
            return fieldB;
        }
	public void setFieldB(TstBeanIgnoreProperty fieldB) {
            this.fieldB = fieldB;
        }
    }
    
    static class TstBeanIgnoreProperty {
	private String fieldA;
	private String fieldB;
	private TstBeanIgnoreProperty fieldC;
	
	public TstBeanIgnoreProperty getFieldC() {
            return fieldC;
        }
	public void setFieldC(TstBeanIgnoreProperty fieldC) {
            this.fieldC = fieldC;
        }
	public String getFieldA() {
            return fieldA;
        }
	public void setFieldA(String fieldA) {
            this.fieldA = fieldA;
        }
	public String getFieldB() {
            return fieldB;
        }
	public void setFieldB(String fieldB) {
            this.fieldB = fieldB;
        }
	
    }
    
    static class TstBeanComplexProperty {
	private TstBeanMultiArgCtor fieldComplex;

	public TstBeanMultiArgCtor getFieldComplex() {
            return fieldComplex;
        }

	public void setFieldComplex(TstBeanMultiArgCtor fieldComplex) {
            this.fieldComplex = fieldComplex;
        }
	
    }
    
    static class TstBeanSelf{
	private TstBeanSelf fieldSelf;

	public TstBeanSelf getFieldSelf() {
            return fieldSelf;
        }

	public void setFieldSelf(TstBeanSelf fieldSelf) {
            this.fieldSelf = fieldSelf;
        }
	
    }
    
    static class TstBeanEnum {
	private static enum TstEnum {
	    ONE,TWO,THREE;
	}
	
	TstEnum enumField;

	public TstEnum getEnumField() {
            return enumField;
        }

	public void setEnumField(TstEnum enumField) {
            this.enumField = enumField;
        }
    }
    
    static class TestBeanArray {
	private String[] stringArray;
	private Float[] floatArray;
	private Character[] charArray;
	public Float[] getFloatArray() {
            return floatArray;
        }
	public void setFloatArray(Float[] floatArray) {
            this.floatArray = floatArray;
        }
	public Integer[] getIntegerArray() {
            return integerArray;
        }
	public void setIntegerArray(Integer[] integerArray) {
            this.integerArray = integerArray;
        }
	public Boolean[] getBooleanArray() {
            return booleanArray;
        }
	public void setBooleanArray(Boolean[] booleanArray) {
            this.booleanArray = booleanArray;
        }
	public Long[] getLongArray() {
            return longArray;
        }
	public void setLongArray(Long[] longArray) {
            this.longArray = longArray;
        }
	public Double[] getDoubleArray() {
            return doubleArray;
        }
	public void setDoubleArray(Double[] doubleArray) {
            this.doubleArray = doubleArray;
        }
	public BigDecimal[] getBigDecimalArray() {
            return bigDecimalArray;
        }
	public void setBigDecimalArray(BigDecimal[] bigDecimalArray) {
            this.bigDecimalArray = bigDecimalArray;
        }
	public BigInteger[] getBigIntegerArray() {
            return bigIntegerArray;
        }
	public void setBigIntegerArray(BigInteger[] bigIntegerArray) {
            this.bigIntegerArray = bigIntegerArray;
        }
	private Integer[] integerArray;
	private Boolean[] booleanArray;
	private Long[] longArray;
	private Double[] doubleArray;
	private BigDecimal[] bigDecimalArray;
	private BigInteger[] bigIntegerArray;
	
	private TstBeanNoArgCtor[] beanArray;
	public String[] getStringArray() {
            return stringArray;
        }
	public void setStringArray(String[] stringArray) {
            this.stringArray = stringArray;
        }
	
	public Character[] getCharArray() {
            return charArray;
        }
	public void setCharArray(Character[] charArray) {
            this.charArray = charArray;
        }
	public TstBeanNoArgCtor[] getBeanArray() {
            return beanArray;
        }
	public void setBeanArray(TstBeanNoArgCtor[] beanArray) {
            this.beanArray = beanArray;
        }
	
    }

    public static class TstBeanSetters {
	private Map<String, Object> fieldToValues = new HashMap<String, Object>();

	private int setterInvokeCount = 0;

	private void set(String fieldName, Object val) {
	    fieldToValues.put(fieldName, val);
	    setterInvokeCount++;
	}

	int testGetSetterInvokeCount() {
	    return setterInvokeCount;
	}

	public void setPrimitiveBoolean(boolean val) {
	    set("boolean", val);
	}

	public void setPrimitiveByte(byte val) {
	    set("byte", val);
	}

	public void setPrimitiveChar(char val) {
	    set("char", val);
	}

	public void setPrimitiveShort(short val) {
	    set("short", val);
	}

	public void setPrimitiveInt(int val) {
	    set("int", val);
	}

	public void setPrimitiveLong(long val) {
	    set("long", val);
	}

	public void setPrimitiveFloat(float val) {
	    set("float", val);
	}

	public void setPrimitiveDouble(double val) {
	    set("double", val);
	}

	public void setString(String val) {
	    set("String", val);
	}

	public void setBoolean(Boolean val) {
	    set("Boolean", val);
	}

	public void setByte(Byte val) {
	    set("Byte", val);
	}

	public void setCharacter(Character val) {
	    set("Character", val);
	}

	public void setShort(Short val) {
	    set("Short", val);
	}

	public void setInteger(Integer val) {
	    set("Integer", val);
	}

	public void setLong(Long val) {
	    set("Long", val);
	}

	public void setFloat(Float val) {
	    set("Float", val);
	}

	public void setDouble(Double val) {
	    set("Double", val);
	}

	public void setBigDecimal(BigDecimal val) {
	    set("BigDecimal", val);
	}

	public void setBigInteger(BigInteger val) {
	    set("BigInteger", val);
	}
    }

    private static class TstBeanMultiArgCtor {
	private final String fieldA;
	private final Integer fieldB;

	public TstBeanMultiArgCtor(String fieldA, int fieldB) {
	    super();
	    this.fieldA = fieldA;
	    this.fieldB = fieldB;
	}

	public String getFieldA() {
	    return fieldA;
	}

	public Integer getFieldB() {
	    return fieldB;
	}
    }

    public static class TstBeanPrivateNoArgCtor {

    }

    public static class TstBeanNoArgCtor {

    }

}
