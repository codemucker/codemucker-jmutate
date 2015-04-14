package org.codemucker.jmutate.ast;

import static org.codemucker.jmatch.Assert.assertThat;
import static org.codemucker.jmatch.Assert.isEqualTo;

import java.util.List;

import org.codemucker.jmatch.AMap;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.TestSourceHelper;
import org.codemucker.jmutate.ast.sub_package.EnumFromOtherPackage;
import org.codemucker.jpattern.generate.IsGenerated;
import org.junit.Test;

public class JAnnotationTest {

    @Test
	public void resolvesName(){
		JType type = TestSourceHelper.findSourceForClass(JAnnotationTest.class).getTypeWithName(TestBean.class);
		List<JAnnotation> annons = type.getAnnotations().getAllIncludeNested();
		
		assertThat(annons.size(),isEqualTo(1));
	
		JAnnotation anon = annons.iterator().next();
		
		assertThat(anon.getQualifiedName(),isEqualTo(IsGenerated.class.getName()));
		assertThat(anon.isOfType(IsGenerated.class));
	}
    
    @Test
    public void isAnnotationPresentCOmpiled(){
    	//todo
    }
	
    public static class TestBean {
		
		@IsGenerated(by="meh")
		public void myMethod(){
			
		}
	}
	
	
	private @interface MyTestAnnotation {
	    
	    @interface NestedAnnotation {
	        String nestedName() default "";
	    }
	}
	
	
    @Test
	public void extractAttributeValues(){
		JAnnotation annon = TestSourceHelper.findSourceForClass(JAnnotationTest.class).getTypeWithName(Test2Bean.class).getAnnotations().getOrNull(Test2Annotation.class);
		
		assertThat(annon.getAttributeValue("att3").toString(),isEqualTo("abcd"));
		assertThat(annon.getAttributeValue("att4").toString(),isEqualTo("7"));
		assertThat(annon.getAttributeValue("att5").toString(),isEqualTo(Test2Constants.class.getName().replace('$', '.')));
		assertThat(annon.getAttributeValue("att6").toString(),isEqualTo("bar"));
		//assertThat(annon.getAttributeValue("att7").toString(),isEqualTo("Foo"));
		assertThat(annon.getAttributeValue("att8").toString(),isEqualTo("org.codemucker.jmutate.ast.JAnnotationTest.MyEnum.Bar"));
	    
		assertThat(annon.getAttributeValue("att2","mydefault").toString(),isEqualTo("mydefault"));
		
		Expect.that(annon.getAttributeMap()).is(AMap.ofStringObject().inAnyOrder().withOnly("att3", "abcd").and("att4","7").andKey("att5").and("att6","bar").and("att8","org.codemucker.jmutate.ast.JAnnotationTest.MyEnum.Bar"));
		
    }
	
    
    
	@Test2Annotation(
			att3="abcd",
			att4=7,
			att5=Test2Constants.class,
			att6= Test2Constants.FOO,
			att8=MyEnum.Bar
	)
    public class Test2Bean {
		
	}
	
	public static class Test2Constants {
		public static final String FOO = "bar";
	}
	
    private @interface Test2Annotation {
    	String att1() default "";
    	String att2() default "1234";
    	String att3() default "";
    	int att4() default 0;
    	Class<?> att5() default Object.class;
    	String att6() default "";
    	MyEnum att7() default MyEnum.Foo;
    	MyEnum att8();
    }
    
    private enum MyEnum {
    	Foo,Bar
    }
    
    @Test
	public void bugEnumFromOtherPackage(){
		JAnnotation annon = TestSourceHelper.findSourceForClass(JAnnotationTest.class).getTypeWithName(Test3Bean.class).getAnnotations().getOrNull(Test3Annotation.class);

		Expect
			.that(annon.getAttributeMap())
			.is(AMap.ofStringObject()
					.withOnly("att", EnumFromOtherPackage.class.getName() + ".TWO"));

		
    }
    
    @Test3Annotation(att=EnumFromOtherPackage.TWO)
    public class Test3Bean {
		
   	}
    
    private @interface Test3Annotation {
    	EnumFromOtherPackage att();
    }
	
}
