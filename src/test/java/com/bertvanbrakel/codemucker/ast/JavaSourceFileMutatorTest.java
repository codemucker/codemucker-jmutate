package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.junit.Test;

import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.test.util.TestHelper;

public class JavaSourceFileMutatorTest {

	TestHelper helper = new TestHelper();
	MutationContext context = new SimpleMutationContext();
	
	@Test
	public void testGetMainType() throws Exception {
		SourceTemplate t = context.newSourceTemplate();
		t.println("package foo.bar;");
		t.println("public class Foo {");
		t.println("}");
		t.println("public class Alice {");
		t.println("}");
		t.println("public class Bob {");
		t.println("}");

		JSourceFile srcFile = t.asSourceFileWithFullName("foo.bar.Alice");
		
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
		t.println("public class Alice {");
		t.println("}");
		t.println("public class Bob {");
		t.println("}");

		//TODO:Hmm, what is going on here? looks like ti test that itself is itself...
		JSourceFileMutator srcFile = t.asSourceFileWithFullName("foo.bar.Alice").asMutator(context);
		
		JTypeMutator mutable = srcFile.getMainTypeAsMutable();
		AbstractTypeDeclaration type = srcFile.getJSource().getMainType().getAstNode();
		
		assertNotNull(mutable);
		assertEquals(mutable.getJType().asTypeDecl(), type);
	}
}
