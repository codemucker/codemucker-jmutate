package com.bertvanbrakel.codemucker.util;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.SimpleCodeMuckContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;

public class JavaNameUtilTest {

	SimpleCodeMuckContext ctxt = new SimpleCodeMuckContext();
	
	@Test
	@Ignore("Currently don't support this. Have a tmp workaround for method clash dectection issue")
	//bug where interfaces declared on types are not being resolved correctly
	public void test_getFQDN_onInterfacesDeclaredInSamePackageMembers(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.v("pkg", JavaNameUtilTest.class.getPackage().getName());
		t.pl("package ${pkg};");
		t.pl("class MyType {");
		t.pl("public MyInterface myField;");
		t.pl("}");
		
		JField field = t.asResolvedSourceFileNamed("${pkg}.MyType").getMainType().findAllFields().getFirst();
		
		assertEquals(MyInterface.class.getName(), JavaNameUtil.getQualifiedName(field.getType()));
	}
	
	public static interface MyInterface {
	};
	
}
