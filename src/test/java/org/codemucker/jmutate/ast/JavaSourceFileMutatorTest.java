package org.codemucker.jmutate.ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.codemucker.jmutate.transform.CodeMuckContext;
import org.codemucker.jmutate.transform.SourceTemplate;
import org.codemucker.jtest.TestHelper;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.junit.Test;


public class JavaSourceFileMutatorTest {

	TestHelper helper = new TestHelper();
	CodeMuckContext context = new SimpleCodeMuckContext();
	
	@Test
	public void testGetMainTypeAsResolved() throws Exception {
		SourceTemplate t = context.newSourceTemplate();
		t.println("package foo.bar;");
		t.println("class Foo {}");
		t.println("class Alice {}");
		t.println("class Bob {}");

		JSourceFile srcFile = t.asResolvedSourceFileNamed("foo.bar.Alice");
		
		JType type = srcFile.getMainType();
		assertNotNull(type);
		assertEquals(type.getSimpleName(), "Alice");
	}
	
	@Test
	public void testGetMainTypeAsSnippet() throws Exception {
		SourceTemplate t = context.newSourceTemplate();
		t.println("package foo.bar;");
		t.println("class Foo {}");
		t.println("public class Alice {}");
		t.println("class Bob {}");

		JSourceFile srcFile = t.asSourceFileSnippet();
		
		JType type = srcFile.getMainType();
		assertNotNull(type);
		assertEquals(type.getSimpleName(), "Alice");
	}
	
	@Test
	public void testGetMainTypeAsMutable() throws Exception {
		SourceTemplate t = context.newSourceTemplate();
		t.println("package foo.bar;");
		t.println("public class Foo {");
		t.println("}");
		t.println("class Alice {");
		t.println("}");
		t.println("class Bob {");
		t.println("}");

		//TODO:Hmm, what is going on here? looks like ti test that itself is itself...
		JSourceFileMutator srcFile = t.asResolvedSourceFileNamed("foo.bar.Foo").asMutator(context);
		
		JTypeMutator mutable = srcFile.getMainTypeAsMutable();
		AbstractTypeDeclaration type = srcFile.getJSource().getMainType().asAbstractTypeDecl();
		
		assertNotNull(mutable);
		assertEquals(mutable.getJType().asTypeDecl(), type);
		assertEquals("foo.bar.Foo", mutable.getJType().getFullName());
	}
}
