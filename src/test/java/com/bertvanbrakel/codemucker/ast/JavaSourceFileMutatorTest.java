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
	MutationContext context = new DefaultMutationContext();
	
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

		JSourceFile srcFile = t.asSourceFileWithFQN("foo.bar.Alice");
		
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

		JSourceFileMutator srcFile = t.asSourceFileWithFQN("foo.bar.Alice").asMutator(context);
		
		JTypeMutator mutable = srcFile.getMainTypeAsMutable();
		AbstractTypeDeclaration type = srcFile.getJSource().getMainType().getTypeNode();
		
		assertNotNull(mutable);
		assertEquals(mutable.getJType().asType(), type);
	}
}
