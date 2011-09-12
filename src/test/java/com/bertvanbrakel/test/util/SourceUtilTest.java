package com.bertvanbrakel.test.util;

import static com.bertvanbrakel.test.util.SourceUtil.assertSourceFileAstsMatch;
import static com.bertvanbrakel.test.util.SourceUtil.getAstFromClassBody;
import static com.bertvanbrakel.test.util.SourceUtil.writeNewJavaFile;

import java.io.File;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

public class SourceUtilTest {

	@Test
	public void test_sameSrcFileMatches() throws Exception {
		SrcWriter src = new SrcWriter();
		src.append("package com.bertvanbrakel.test;");
		src.append("public class TestBeanModify {");
		src.append("private String myField;");
		src.append("private String myField2;");
		src.append("\npublic void foo(){ this.myField = null; }");
		src.append("\n}");

		File srcFile = writeNewJavaFile(src);
		File srcFile2 = writeNewJavaFile(src);

		assertSourceFileAstsMatch(srcFile, srcFile2);
	}

	@Test
	public void test_sameSrcFileMatchesWithDifferentSpacing() throws Exception {
		SrcWriter scr1 = new SrcWriter();
		scr1.print("package com.bertvanbrakel.test;");
		scr1.print("public class TestBeanModify {");
		scr1.print("private String myField;");
		scr1.print("private String myField2;");
		scr1.print("\npublic void foo(){ this.myField = null; }");
		scr1.print("}");

		SrcWriter src2 = new SrcWriter();
		src2.println("package com.bertvanbrakel.test;");
		src2.println();
		src2.println("public class TestBeanModify {");
		src2.println();
		src2.println("private String\tmyField;");
		src2.println();
		src2.println("private String   myField2;");
		src2.println();
		src2.println("public\tvoid\t foo(\t){\tthis.myField = null; }");
		src2.println();
		src2.println("}");

		File srcFile = writeNewJavaFile(scr1);
		File srcFile2 = writeNewJavaFile(src2);

		assertSourceFileAstsMatch(srcFile, srcFile2);
	}

	@Test
	public void test_can_parse_just_method() throws Exception {
		SrcWriter src1 = new SrcWriter();
		// src1.println( "import foo.com.Bar;");
		src1.println("private String myField;");
		src1.println("public void foo(){}");

		TypeDeclaration node = getAstFromClassBody(src1.getSource());

		System.out.println(node);
		System.out.println(node.bodyDeclarations());
		node.bodyDeclarations();
		System.out.println(ToStringBuilder.reflectionToString(node, ToStringStyle.MULTI_LINE_STYLE));

	}
}
