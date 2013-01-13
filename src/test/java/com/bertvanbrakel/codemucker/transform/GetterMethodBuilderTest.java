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
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.SimpleCodeMuckContext;
import com.bertvanbrakel.codemucker.util.SourceAsserts;

public class GetterMethodBuilderTest {

	CodeMuckContext ctxt = new SimpleCodeMuckContext();
	
	@Test
	public void test_default_create(){
		JMethod actual = ctxt.obtain(GetterMethodBuilder.class)
			.setFieldName("myField")
			.setFieldType("my.org.Foo")
			.build();
	
		JMethod expect = ctxt.newSourceTemplate()
    		.pl("public my.org.Foo getMyField(){ return this.myField; }")
    		.asResolvedJMethod();
    	
    	SourceAsserts.assertAstsMatch(expect,actual);
	}
	
	@Test
	public void test_non_default() throws Exception {
		JMethod actual = ctxt.obtain(GetterMethodBuilder.class)
			.setMethodAccess(JAccess.PROTECTED)
			.setMarkedGenerated(true)
			.setFieldName("myField")
			.setFieldType("my.org.Foo")
			.build();
	
		JMethod expect = ctxt.newSourceTemplate()
			.v("pattern", Pattern.class)
			.p("@${pattern}(name=\"").p("bean.getter").pl("\")")
			.pl("protected my.org.Foo getMyField(){return this.myField;}")
    		.asResolvedJMethod();
    	
    	SourceAsserts.assertAstsMatch(expect,actual);
	}
}
