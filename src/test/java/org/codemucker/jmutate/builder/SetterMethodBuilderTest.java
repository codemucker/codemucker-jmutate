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
package org.codemucker.jmutate.builder;

import org.codemucker.jmutate.MutateContext;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.SimpleMutateContext;
import org.codemucker.jmutate.builder.JFieldBuilder;
import org.codemucker.jmutate.util.SourceAsserts;
import org.codemucker.jpattern.Pattern;
import org.junit.Test;


public class SetterMethodBuilderTest {

	MutateContext ctxt = SimpleMutateContext.with().defaults().build();
	
	@Test
	public void test_create_with_defaults() throws Exception {
		
		JField actual = ctxt.obtain(JFieldBuilder.class)
			.fieldType("String")
			.fieldName("myField")
			.build();
		
		JField expect = ctxt.newSourceTemplate()
			.pl( "private String myField;" )
			.asResolvedJField();
		
		SourceAsserts.assertAstsMatch(expect,actual);
	}
	
	@Test
	public void test_create_non_defaults() throws Exception {
		
		JField actual = ctxt.obtain(JFieldBuilder.class)
			.fieldAccess(JAccess.PUBLIC)
			.pattern("my.pattern")
			.markedGenerated(true)
			.fieldType("java.lang.Object")
			.fieldName("myField")
			.build();
		
		JField expect = ctxt.newSourceTemplate()
			.pl('@').p(Pattern.class.getName()).p("(name=\"my.pattern\")").p( "public Object myField;" ).pl()
			.asResolvedJField();
		
		SourceAsserts.assertAstsMatch(expect,actual);
	}
}
