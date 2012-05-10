package com.bertvanbrakel.codemucker.transform;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JFieldMatchers;
import com.bertvanbrakel.codemucker.util.SourceAsserts;

public class FieldSplitterTransformTest {

	MutationContext ctxt = new SimpleMutationContext();
	
	@Test
	public void test_split_with_string_initializer(){
	
		JType actual = ctxt.newSourceTemplate()
			.pl("class Foo{")
			.pl("	String a=\"val1\",b,c=\"val2\";")
			.pl("}")
			.asJType();
		
		JField field = actual.findFieldsMatching(JFieldMatchers.withName("a")).getFirst();
		
		ctxt.obtain(FieldSplitterTransform.class)
			.setTarget(actual)
			.setField(field)
			.apply();
		
		JType expected = ctxt.newSourceTemplate()
			.pl("class Foo{")
			.pl("	String a=\"val1\";")
			.pl("	String b;")
			.pl("	String c=\"val2\";")
			.pl("}")
			.asJType();
		
		SourceAsserts.assertAstsMatch(expected, actual);
	}
	
	@Test
	public void test_split_with_expression_initializer(){
		
		JType actual = ctxt.newSourceTemplate()
			.pl("class Foo{")
			.pl("	int a=1*2,b,c=10+3;")
			.pl("}")
			.asJType();
		
		JField field = actual.findFieldsMatching(JFieldMatchers.withName("a")).getFirst();
		
		ctxt.obtain(FieldSplitterTransform.class)
			.setTarget(actual)
			.setField(field)
			.apply();
		
		JType expected = ctxt.newSourceTemplate()
			.pl("class Foo{")
			.pl("	int a=1*2;")
			.pl("	int b;")
			.pl("	int c=10+3;")
			.pl("}")
			.asJType();
		
		SourceAsserts.assertAstsMatch(expected, actual);
	}
}
