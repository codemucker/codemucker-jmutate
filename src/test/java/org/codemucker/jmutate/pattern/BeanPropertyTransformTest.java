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
import org.codemucker.jmutate.ast.SimpleMutateContext;
import org.codemucker.jmutate.transform.MutateContext;
import org.codemucker.jmutate.util.SourceAsserts;
import org.codemucker.jpattern.Property;
import org.codemucker.jpattern.Pattern;
import org.junit.Test;


public class BeanPropertyTransformTest {

	private MutateContext ctxt = SimpleMutateContext.builder()
			.markGenerated(true)
			.build();
		
	@Test
	public void testTransformNoPreExisting() throws Exception {
		
		//given
		JType target = beanWithNoProperties(ctxt);
		//when
		whenAPropertyTransformIsApplied(ctxt, target);
		//then
		JType expectType = expectBeanWithProperty(ctxt);
    	SourceAsserts.assertRootAstsMatch(expectType,target);
	}

	private void whenAPropertyTransformIsApplied(MutateContext ctxt, JType target) {
		ctxt.obtain(BeanPropertyTransform.class)
			.setTarget(target)
			.setPropertyName("myField")
			.setPropertyType("String")
			.transform();
	}

	private JType beanWithNoProperties(MutateContext ctxt) {
		JType target = ctxt.newSourceTemplate()
				.pl("package com.mypkg.codegen.bean;")
				.pl("import " + Property.class.getName() + ";")
				.pl( "public class TestBeanModify {")
				.pl("}")
				.asResolvedJTypeNamed("TestBeanModify");
		return target;
	}

	private JType expectBeanWithProperty(MutateContext ctxt) {
		JType expectType = ctxt.newSourceTemplate()
    		.pl("package com.mypkg.codegen.bean;")
    		.pl("import " + Property.class.getName() + ";")
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
