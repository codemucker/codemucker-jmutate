package com.bertvanbrakel.codemucker.transform;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.SimpleCodeMuckContext;
import com.bertvanbrakel.codemucker.ast.matcher.AJField;
import com.bertvanbrakel.codemucker.util.SourceAsserts;

public class FieldSplitterTransformTest {

	CodeMuckContext ctxt = new SimpleCodeMuckContext();
	
	@Test
	public void test_split_with_string_initializer(){
	
		JType actual = ctxt.newSourceTemplate()
			.pl("class Foo{")
			.pl("	String a=\"val1\",b,c=\"val2\";")
			.pl("}")
			.asResolvedJTypeNamed("Foo");
		
		JField field = actual.findFieldsMatching(AJField.withName("a")).getFirst();
		
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
		
		JField field = actual.findFieldsMatching(AJField.withName("a")).getFirst();
		
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
			.asResolvedJTypeNamed("Foo");
		
		SourceAsserts.assertAstsMatch(expected, actual);
	}
}
