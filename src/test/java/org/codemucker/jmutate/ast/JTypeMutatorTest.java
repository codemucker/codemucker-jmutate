package org.codemucker.jmutate.ast;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.codemucker.jmutate.SourceHelper;
import org.codemucker.jmutate.ast.a.TestBean;
import org.codemucker.jmutate.ast.a.TestBeanSimple;
import org.codemucker.jmutate.transform.CodeMuckContext;
import org.codemucker.jmutate.util.SourceAsserts;
import org.codemucker.jtest.TestHelper;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Ignore;
import org.junit.Test;


public class JTypeMutatorTest {

	private CodeMuckContext context = new SimpleCodeMuckContext();
	
	@Test
	public void testAddSimpleField() throws Exception {		
		JTypeMutator type = getMutatorFor(TestBeanSimple.class);
		type.addField("private String foo;");
		assertAstEquals("TestBeanSimple.java.testAddSimpleField", type);
	}
	
	@Test
	public void testAddFieldAndMethods() throws Exception {		
		JTypeMutator type = getMutatorFor(TestBeanSimple.class);
		type.addField("private String foo;");
		type.addMethod("public void setFoo(String foo){ this.foo = foo; }");
		
		assertAstEquals("TestBeanSimple.java.testAddFieldAndMethods", type);
	}
	
	@Test
	public void testAddFieldMethodsWithInnerClasses() throws Exception {
		JTypeMutator type = getMutatorFor(TestBean.class);
		type.addField("private String foo;");
		type.addMethod("public String getFoo(){ return this.foo; }");
		type.addMethod("public void setFoo(String foo){ this.foo = foo; }");
		type.addCtor("public TestBean(String foo){ this.foo = foo; }");
		
		assertAstEquals("TestBean.java.testAddFieldMethodsWithInnerClasses", type);
	}
	
	@Ignore("need to reimplement replacement")
	@Test
	public void testAddDuplicateSimpleField() throws Exception {		
		JTypeMutator type = getMutatorFor(TestBeanSimple.class);
		//TOOD:make this work again!	type.addField("private int fieldOne;").replace(true).apply();
		
		assertAstEquals("TestBeanSimple.java.testAddDuplicateSimpleField", type);
	}
	
	private JTypeMutator getMutatorFor(Class<?> klass){
		return SourceHelper.findSourceForTestClass(klass).asMutator(context).getMainTypeAsMutable();
	}
	
	private void assertAstEquals(String expectPath, JTypeMutator actual){
		TestHelper helper = new TestHelper();
		//read the expected result
		//JavaSourceFile srcFile = actual.getJavaType().getSource();
		String expectSrc = null;
        try {
	        expectSrc = helper.getTestJavaSourceDir().childResource(TestBean.class, expectPath).readAsString();
        } catch (IOException e) {
	        fail("Couldn't read source file " + expectPath);
        }
        CompilationUnit expectCu = context.newSourceTemplate()
        	.setTemplate(expectSrc)
        	.asResolvedCompilationUnitNamed(null);
        
        SourceAsserts.assertAstsMatch(expectCu, expectCu);
    }
}
