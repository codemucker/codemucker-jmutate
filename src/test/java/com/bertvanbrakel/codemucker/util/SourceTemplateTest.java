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

import static com.bertvanbrakel.codemucker.util.SourceUtil.assertAstsMatch;
import static com.bertvanbrakel.codemucker.util.SourceUtil.writeResource;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import junit.framework.AssertionFailedError;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.test.finder.ClassPathResource;

public class SourceTemplateTest {

	private MutationContext ctxt = new SimpleMutationContext();
	
	@Test
	public void test_sameSrcFileMatches() throws Exception {
		SourceTemplate src = ctxt.newSourceTemplate();
		src.println("package com.bertvanbrakel.test;");
		src.println("public class TestBeanModify {");
		src.println("private String myField;");
		src.println("private String myField2;");
		src.println("public void foo(){ this.myField = null; }");
		src.println("}");

		ClassPathResource srcFile = writeResource(src);
		ClassPathResource srcFile2 = writeResource(src);

		assertAstsMatch(srcFile, srcFile2);
	}

	@Test
	public void test_sameSrcFileMatchesWithDifferentSpacing() throws Exception {
		SourceTemplate src1 = ctxt.newSourceTemplate();
		src1.print("package com.bertvanbrakel.test;");
		src1.print("public class TestBeanModify {");
		src1.print("private String myField;");
		src1.print("private String myField2;");
		src1.println("public void foo(){ this.myField = null; }");
		src1.print("}");

		SourceTemplate src2 = ctxt.newSourceTemplate();
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

		ClassPathResource srcFile = writeResource(src1);
		ClassPathResource srcFile2 = writeResource(src2);

		assertAstsMatch(srcFile, srcFile2);
	}
	
	@Test
	public void test_srcFileDOesNotMatch_whenDifferent() throws Exception{	
		SourceTemplate src1 = ctxt.newSourceTemplate();
		src1.println("package com.bertvanbrakel.test;");
		src1.println("public class TestBeanModify {");
		src1.println("private String myField;");
		src1.println("public void foo(){ this.myField = null; }");
		src1.println("}");
		
		SourceTemplate src2 = ctxt.newSourceTemplate();
		src2.println("package com.bertvanbrakel.test;");
		src2.println("public class TestBeanModify {");
		src2.println("private String myField;");
		src2.println("public void foo(){ this.myField = \"\"; }");
		src2.println("}");

		ClassPathResource srcFile = writeResource(src1);
		ClassPathResource srcFile2 = writeResource(src2);

		Throwable expect = null;
		try {
			assertAstsMatch(srcFile, srcFile2);
		} catch (AssertionFailedError e){
			expect = e;
		}
		assertNotNull("Expected exception",expect);
	}
	@Test
	public void test_can_parse_just_method() throws Exception {
		SourceTemplate src1 = ctxt.newSourceTemplate();
		// src1.println( "import foo.com.Bar;");
		src1.println("private String myField;");
		src1.println("public void foo(){}");

		MethodDeclaration node = src1.asMethodNode();
		assertNotNull(node);
		assertEquals("foo",node.getName().getFullyQualifiedName());
	}
}
