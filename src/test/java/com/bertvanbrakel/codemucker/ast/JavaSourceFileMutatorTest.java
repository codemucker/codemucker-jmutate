package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.junit.Test;

import com.bertvanbrakel.codemucker.util.SourceUtil;
import com.bertvanbrakel.codemucker.util.SrcWriter;
import com.bertvanbrakel.test.util.TestHelper;

public class JavaSourceFileMutatorTest {

	TestHelper helper = new TestHelper();

	@Test
	public void testGetMainType() throws Exception {
		SrcWriter w = new SrcWriter();
		w.println("package foo.bar");
		w.println("public class Foo {");
		w.println("}");
		w.println("public class Alice {");
		w.println("}");
		w.println("public class Bob {");
		w.println("}");

		JSourceFile srcFile = newJavaSrc(w, "foo.bar.Alice");
		
		JType type = srcFile.getMainType();
		assertNotNull(type);
		assertEquals(type.getSimpleName(), "Alice");
	}
	
	@Test
	public void testGetMainTypeAsMutable() throws Exception {
		SrcWriter w = new SrcWriter();
		w.println("package foo.bar");
		w.println("public class Foo {");
		w.println("}");
		w.println("public class Alice {");
		w.println("}");
		w.println("public class Bob {");
		w.println("}");

		JSourceFileMutator srcFile = newMutator(w, "foo.bar.Alice");
		
		JTypeMutator mutable = srcFile.getMainTypeAsMutable();
		AbstractTypeDeclaration type = srcFile.getJavaSourceFile().getMainType().getTypeNode();
		
		assertNotNull(mutable);
		assertEquals(mutable.getJavaType().asType(), type);
	}
	
	private JSourceFileMutator newMutator(SrcWriter writer, String fqClassName) {
		return new JSourceFileMutator(newJavaSrc(writer, fqClassName));
	}	
	
	private JSourceFile newJavaSrc(SrcWriter writer, String fqClassName) {
		return SourceUtil.writeJavaSrc(writer, fqClassName);
	}
}
