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
package org.codemucker.jmutate.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Test;


public class SourceAssertsTest {

	private JMutateContext ctxt = DefaultMutateContext.with().defaults().build();
	
	@Test
	public void test_sameSrcFileMatches() throws Exception {
		SourceTemplate src = ctxt.newSourceTemplate();
		src.println("package org.codemucker.jtest;");
		src.println("public class TestBeanModify {");
		src.println("private String myField;");
		src.println("private String myField2;");
		src.println("public void foo(){ this.myField = null; }");
		src.println("}");

		SourceAsserts.assertAstsMatch(src.asSourceFileSnippet(), src.asSourceFileSnippet());
	}

	@Test
	public void test_sameSrcFileMatchesWithDifferentSpacing() throws Exception {
		SourceTemplate t1 = ctxt.newSourceTemplate();
		t1.print("package org.codemucker.jtest;");
		t1.print("public class TestBeanModify {");
		t1.print("private String myField;");
		t1.print("private String myField2;");
		t1.println("public void foo(){ this.myField = null; }");
		t1.print("}");

		SourceTemplate t2 = ctxt.newSourceTemplate();
		t2.println("package org.codemucker.jtest;");
		t2.println();
		t2.println("public class \tTestBeanModify \t{");
		t2.println();
		t2.println("private String\tmyField;");
		t2.println();
		t2.println("private String   myField2;");
		t2.println();
		t2.println("public\tvoid\t foo(\t){\tthis.myField = null; }");
		t2.println();
		t2.println("}");

		SourceAsserts.assertAstsMatch(t1.asSourceFileSnippet(), t2.asSourceFileSnippet());
	}
	
	@Test
	public void test_srcFileDoesNotMatch_whenDifferent() throws Exception{	
		SourceTemplate t1 = ctxt.newSourceTemplate();
		t1.println("package org.codemucker.jtest;");
		t1.println("public class TestBeanModify {");
		t1.println("private String myField;");
		t1.println("public void foo(){ this.myField = null; }");
		t1.println("}");
		
		SourceTemplate t2 = ctxt.newSourceTemplate();
		t2.println("package org.codemucker.jtest;");
		t2.println("public class TestBeanModify {");
		t2.println("private String myField;");
		t2.println("public void foo(){ this.myField = \"\"; }");
		t2.println("}");

		Throwable expect = null;
		try {
			SourceAsserts.assertAstsMatch(t1.asSourceFileSnippet(), t2.asSourceFileSnippet());
		} catch (AssertionError e){
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

		MethodDeclaration node = src1.asResolvedMethodNode();
		assertNotNull(node);
		assertEquals("foo",node.getName().getFullyQualifiedName());
	}
}
