/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bertvanbrakel.codemucker.util;

import static com.bertvanbrakel.codemucker.util.SourceUtil.assertSourceFileAstsMatch;
import static com.bertvanbrakel.codemucker.util.SourceUtil.getAstFromClassBody;
import static com.bertvanbrakel.codemucker.util.SourceUtil.writeNewJavaFile;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

import com.bertvanbrakel.codemucker.util.SrcWriter;

public class SrcWriterTest {

	@Test
	public void test_sameSrcFileMatches() throws Exception {
		SrcWriter src = new SrcWriter();
		src.append("package com.bertvanbrakel.test;");
		src.append("public class TestBeanModify {");
		src.append("private String myField;");
		src.append("private String myField2;");
		src.append("\npublic void foo(){ this.myField = null; }");
		src.append("\n}");

		File srcFile = writeNewJavaFile(src).getFile();
		File srcFile2 = writeNewJavaFile(src).getFile();

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

		File srcFile = writeNewJavaFile(scr1).getFile();
		File srcFile2 = writeNewJavaFile(src2).getFile();

		assertSourceFileAstsMatch(srcFile, srcFile2);
	}
	
	@Test
	public void test_srcFileDOesNotMatch_whenDifferent() throws Exception{	
		SrcWriter src1 = new SrcWriter();
		src1.append("package com.bertvanbrakel.test;");
		src1.append("public class TestBeanModify {");
		src1.append("private String myField;");
		src1.append("\npublic void foo(){ this.myField = null; }");
		src1.append("\n}");
		
		SrcWriter src2 = new SrcWriter();
		src2.append("package com.bertvanbrakel.test;");
		src2.append("public class TestBeanModify {");
		src2.append("private String myField;");
		src2.append("\npublic void foo(){ this.myField = \"\"; }");
		src2.append("\n}");
		

		File srcFile = writeNewJavaFile(src1).getFile();
		File srcFile2 = writeNewJavaFile(src2).getFile();

		Throwable expect = null;
		try {
			assertSourceFileAstsMatch(srcFile, srcFile2);
		} catch (AssertionFailedError e){
			expect = e;
		}
		assertNotNull("Expected exception",expect);
	}
	@Test
	public void test_can_parse_just_method() throws Exception {
		SrcWriter src1 = new SrcWriter();
		// src1.println( "import foo.com.Bar;");
		src1.println("private String myField;");
		src1.println("public void foo(){}");

		TypeDeclaration node = getAstFromClassBody(src1.getSource());

		log(node);
		log(node.bodyDeclarations());
		node.bodyDeclarations();
		log(ToStringBuilder.reflectionToString(node, ToStringStyle.MULTI_LINE_STYLE));

	}
	
	private static void log(Object msg) {
		if (false) {
			System.out.println(msg);
		}
	}
}
