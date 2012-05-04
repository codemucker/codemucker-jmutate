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
package com.bertvanbrakel.codemucker.pattern;

import static com.bertvanbrakel.codemucker.util.SourceUtil.assertAstsMatch;

import org.junit.Test;

import com.bertvanbrakel.codemucker.annotation.BeanProperty;
import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.bertvanbrakel.codemucker.transform.MutationContext;

public class BeanPropertyPatternTest {

	@Test
	public void test_add_field_getter_setter() throws Exception {
		MutationContext ctxt = new SimpleMutationContext();
		
		JType expectType = ctxt.newSourceTemplate()
    		.pl("package com.bertvanbrakel.codegen.bean;")
    		.pl("import " + BeanProperty.class.getName() + ";")
    		.pl( "public class TestBeanModify {")
    		.pl('@').p(Pattern.class.getName()).p("(name=\"bean.property\")").p( "private String myField;" ).nl()
    		.pl("@").p(Pattern.class.getName()).p("(name=\"bean.setter\") public void setMyField(String myField ){ this.myField = myField;}" ).nl()
    		.pl("@").p(Pattern.class.getName()).p("(name=\"bean.getter\") public String getMyField(){ return this.myField;}" ).nl()
    		
    		//.println( "public String getMyField(){ return this.myField;}" )
    		.pl("}")
    		.asJType();
    	
		JType target = ctxt.newSourceTemplate()
			.pl("package com.bertvanbrakel.codegen.bean;")
			.pl("import " + BeanProperty.class.getName() + ";")
			.pl( "public class TestBeanModify {")
			.pl("}")
			.asJType();
		
		new BeanPropertyPattern()
			.setCtxt(ctxt)
			.setTarget(target)
			.setPropertyName("myField")
			.setPropertyType("String")
			.apply();
		
		assertAstsMatch(expectType.getCompilationUnit(),target.getCompilationUnit());
	}
}
