package org.codemucker.jmutate.transform;

import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.util.SourceAsserts;
import org.junit.Test;


public class FieldSplitterTransformTest {

	JMutateContext ctxt = DefaultMutateContext.with().defaults().build();
	
	@Test
	public void test_split_with_string_initializer(){
	
		JType actual = ctxt.newSourceTemplate()
			.pl("class Foo{")
			.pl("	String a=\"val1\",b,c=\"val2\";")
			.pl("}")
			.asResolvedJTypeNamed("Foo");
		
		JField field = actual.findFieldsMatching(AJField.with().name("a")).getFirst();
		
		ctxt.obtain(FieldSplitterTransform.class)
			.setTarget(actual)
			.setField(field)
			.transform();
		
		JType expected = ctxt.newSourceTemplate()
			.pl("class Foo{")
			.pl("	String a=\"val1\";")
			.pl("	String b;")
			.pl("	String c=\"val2\";")
			.pl("}")
			.asResolvedJTypeNamed("Foo");
		
		SourceAsserts.assertAstsMatch(expected, actual);
	}
	
	@Test
	public void test_split_with_expression_initializer(){
		
		JType actual = ctxt.newSourceTemplate()
			.pl("class Foo{")
			.pl("	int a=1*2,b,c=10+3;")
			.pl("}")
			.asResolvedJTypeNamed("Foo");
		
		JField field = actual.findFieldsMatching(AJField.with().name("a")).getFirst();
		
		ctxt.obtain(FieldSplitterTransform.class)
			.setTarget(actual)
			.setField(field)
			.transform();
		
		JType expected = ctxt.newSourceTemplate()
			.pl("class Foo{")
			.pl("	int a=1*2;")
			.pl("	int b;")
			.pl("	int c=10+3;")
			.pl("}")
			.asResolvedJTypeNamed("Foo");
		
		SourceAsserts.assertAstsMatch(expected, actual);
	}
}
