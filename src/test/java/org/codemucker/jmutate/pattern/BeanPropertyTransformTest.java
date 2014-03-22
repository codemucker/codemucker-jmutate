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
package org.codemucker.jmutate.pattern;

import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.SimpleCodeMuckContext;
import org.codemucker.jmutate.transform.CodeMuckContext;
import org.codemucker.jmutate.util.SourceAsserts;
import org.codemucker.jpattern.BeanProperty;
import org.codemucker.jpattern.Pattern;
import org.junit.Test;


public class BeanPropertyTransformTest {

	private CodeMuckContext ctxt = SimpleCodeMuckContext.builder()
			.setMarkGenerated(true)
			.build();
		
	@Test
	public void testTransformNoPreExisting() throws Exception {
		
		//given
		JType target = aBeanWithNoProperties(ctxt);
		//when
		whenAPropertyTransformIsApplied(ctxt, target);
		//then
		JType expectType = aBeanWithProperty(ctxt);
    	SourceAsserts.assertRootAstsMatch(expectType,target);
	}

	private void whenAPropertyTransformIsApplied(CodeMuckContext ctxt, JType target) {
		ctxt.obtain(BeanPropertyTransform.class)
			.setTarget(target)
			.setPropertyName("myField")
			.setPropertyType("String")
			.transform();
	}

	private JType aBeanWithNoProperties(CodeMuckContext ctxt) {
		JType target = ctxt.newSourceTemplate()
				.pl("package com.bertvanbrakel.codegen.bean;")
				.pl("import " + BeanProperty.class.getName() + ";")
				.pl( "public class TestBeanModify {")
				.pl("}")
				.asResolvedJTypeNamed("TestBeanModify");
		return target;
	}

	private JType aBeanWithProperty(CodeMuckContext ctxt) {
		JType expectType = ctxt.newSourceTemplate()
    		.pl("package com.bertvanbrakel.codegen.bean;")
    		.pl("import " + BeanProperty.class.getName() + ";")
    		.pl( "public class TestBeanModify {")
    		.pl('@').p(Pattern.class.getName()).p("(name=\"bean.property\")").p( "private String myField;" ).pl()
    		.pl("@").p(Pattern.class.getName()).p("(name=\"bean.setter\") public void setMyField(String myField ){ this.myField = myField;}" ).pl()
    		.pl("@").p(Pattern.class.getName()).p("(name=\"bean.getter\") public String getMyField(){ return this.myField;}" ).pl()
    		
    		//.println( "public String getMyField(){ return this.myField;}" )
    		.pl("}")
    		.asResolvedJTypeNamed("TestBeanModify");
		return expectType;
	}


}