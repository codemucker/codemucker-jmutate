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
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.bertvanbrakel.codemucker.util.SourceAsserts;

public class InsertFieldTransformTest {

	@Test
	public void test_add_field() throws Exception {
		MutationContext ctxt = new SimpleMutationContext();
		
		SourceTemplate srcBefore = ctxt.newSourceTemplate()
			.pl("package com.bertvanbrakel.codegen.bean;")
			.pl( "public class TestBeanModify {")
			.pl("}");
		
		JType after = srcBefore.asJType();
		
		JField field = FieldBuilder.newBuilder()
			.setContext(ctxt)
			.setMarkedGenerated(true)
			.setPattern("mypattern")
			.setType("String")
			.setName("myField")
			.build();
		
		//this is what we are testing
		InsertFieldTransform.newTransform()
			.setTarget(after)
			.setField(field)
			.setPlacementStrategy(ctxt.create(PlacementStrategies.class).getFieldStrategy())
			.apply();
	
		JType expectType = ctxt.newSourceTemplate()
			.pl("package com.bertvanbrakel.codegen.bean;")
			.pl( "public class TestBeanModify {")
			.pl('@').p(Pattern.class.getName()).p("(name=\"mypattern\")").p( "private String myField;" ).nl()
			.pl("}")
			.asJType();
		
		SourceAsserts.assertAstsMatch(expectType,after);
	}
}
