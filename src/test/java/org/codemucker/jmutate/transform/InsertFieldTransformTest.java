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
package org.codemucker.jmutate.transform;

import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.SimpleCodeMuckContext;
import org.codemucker.jmutate.util.SourceAsserts;
import org.codemucker.jpattern.Pattern;
import org.junit.Test;


public class InsertFieldTransformTest {

	@Test
	public void test_add_field() throws Exception {
		CodeMuckContext ctxt = new SimpleCodeMuckContext();
		
		SourceTemplate srcBefore = ctxt.newSourceTemplate()
			.pl("package com.bertvanbrakel.codegen.bean;")
			.pl( "public class TestBeanModify {")
			.pl("}");
		
		JType after = srcBefore.asResolvedJTypeNamed("com.bertvanbrakel.codegen.bean.TestBeanModify");
		
		JField field = FieldBuilder.builder()
			.setContext(ctxt)
			.setMarkedGenerated(true)
			.setPattern("mypattern")
			.setFieldType("String")
			.setFieldName("myField")
			.build();
		
		//this is what we are testing
		InsertFieldTransform.newTransform()
			.setTarget(after)
			.setField(field)
			.setPlacementStrategy(ctxt.obtain(PlacementStrategies.class).getFieldStrategy())
			.transform();
	
		JType expectType = ctxt.newSourceTemplate()
			.pl("package com.bertvanbrakel.codegen.bean;")
			.pl( "public class TestBeanModify {")
			.pl('@').p(Pattern.class.getName()).p("(name=\"mypattern\")").p( "private String myField;" ).pl()
			.pl("}")
			.asResolvedJTypeNamed("com.bertvanbrakel.codegen.bean.TestBeanModify");
		
		SourceAsserts.assertAstsMatch(expectType,after);
	}
}
