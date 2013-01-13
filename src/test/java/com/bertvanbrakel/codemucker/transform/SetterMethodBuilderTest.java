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
package com.bertvanbrakel.codemucker.transform;

import org.junit.Test;

import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.SimpleCodeMuckContext;
import com.bertvanbrakel.codemucker.util.SourceAsserts;

public class SetterMethodBuilderTest {

	CodeMuckContext ctxt = new SimpleCodeMuckContext();
	
	@Test
	public void test_create_with_defaults() throws Exception {
		
		JField actual = ctxt.obtain(FieldBuilder.class)
			.setFieldType("String")
			.setFieldName("myField")
			.build();
		
		JField expect = ctxt.newSourceTemplate()
			.pl( "private String myField;" )
			.asResolvedJField();
		
		SourceAsserts.assertAstsMatch(expect,actual);
	}
	
	@Test
	public void test_create_non_defaults() throws Exception {
		
		JField actual = ctxt.obtain(FieldBuilder.class)
			.setFieldAccess(JAccess.PUBLIC)
			.setPattern("my.pattern")
			.setMarkedGenerated(true)
			.setFieldType("java.lang.Object")
			.setFieldName("myField")
			.build();
		
		JField expect = ctxt.newSourceTemplate()
			.pl('@').p(Pattern.class.getName()).p("(name=\"my.pattern\")").p( "public java.lang.Object myField;" ).pl()
			.asResolvedJField();
		
		SourceAsserts.assertAstsMatch(expect,actual);
	}
}
