package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.ClasspathResource;
import com.bertvanbrakel.codemucker.util.SrcWriter;
import com.bertvanbrakel.test.util.TestHelper;

public class MutableJavaSourceFileTest {

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

		MutableJavaSourceFile srcFile = newJavaSrc(w, "foo.bar.Alice");
		
		AbstractTypeDeclaration type = srcFile.getMainType();
		assertNotNull(type);
		assertEquals(type.getName().toString(), "Alice");
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

		MutableJavaSourceFile srcFile = newJavaSrc(w, "foo.bar.Alice");
		
		MutableJavaType mutable = srcFile.getMainTypeAsMutable();
		AbstractTypeDeclaration type = srcFile.getMainType();
		
		assertNotNull(mutable);
		assertEquals(mutable.asType(), type);
	}
	
	private MutableJavaSourceFile newJavaSrc(SrcWriter w, String fqClassName) throws IOException {
		File dir = helper.createTempDir();
		String path = fqClassName.replace('.', '/') + ".java";

		File f = new File(dir, path);
		writeSrcTo(w, f);

		MutableJavaSourceFile srcFile = new MutableJavaSourceFile(new JavaSourceFile(new DefaultAstCreator(),
		        new ClasspathResource(dir, path)));

		return srcFile;
	}

	private void writeSrcTo(SrcWriter w, File f) throws IOException {
		f.getParentFile().mkdirs();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			IOUtils.write(w.getSource(), fos);
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}
}
