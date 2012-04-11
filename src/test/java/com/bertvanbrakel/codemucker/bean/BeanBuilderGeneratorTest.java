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
package com.bertvanbrakel.codemucker.bean;

import static com.bertvanbrakel.codemucker.util.SourceUtil.assertSourceFileAstsMatch;
import static com.bertvanbrakel.codemucker.util.SourceUtil.getJavaSourceFrom;
import static com.bertvanbrakel.codemucker.util.SourceUtil.writeResource;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.JTypeMutator;
import com.bertvanbrakel.codemucker.util.SrcWriter;
import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.PropertyDefinition;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.util.ClassNameUtil;

public class BeanBuilderGeneratorTest {

	
	@Test
	public void test_generate() {

		BeanDefinition def = new BeanDefinition(Void.class);
		for (Class<?> type : Arrays.asList(Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Character.TYPE,
		        Character.class, Short.TYPE, Short.class, Integer.TYPE, Integer.class, Long.TYPE, Long.class,
		        Float.TYPE, Float.class, Double.TYPE, Double.class, String.class, Date.class)) {
			addProperty(def, type);
		}

		new BeanBuilderGenerator().generate("com.bertvanbrakel.codegen.bean.AutoBean", def, new GeneratorOptions());
	
	}

	@Test
	public void test_addGetter_and_setter() throws Exception {
		SrcWriter srcBefore = new SrcWriter();
		srcBefore.println("package com.bertvanbrakel.codegen.bean;");
		srcBefore.println( "public class TestBeanModify {");
		srcBefore.println( "@BeanProperty");
		srcBefore.println( "private String myField;" );
		srcBefore.println("}");

		SrcWriter srcExpected = new SrcWriter();
		srcExpected.println("package com.bertvanbrakel.codegen.bean;");
		srcExpected.println( "public class TestBeanModify {");
		srcExpected.println( "@BeanProperty");
		srcExpected.println( "private String myField;" );
		srcExpected.println( "public void setMyField(String myField ){ this.myField = myField;}" );
		srcExpected.println( "public String getMyField(){ return this.myField;}" );
		srcExpected.println("}");

		ClassPathResource modifiedSrcFile = writeResource(srcBefore);
		ClassPathResource srcFileExpected = writeResource(srcExpected);
		
		JSourceFile source = getJavaSourceFrom(modifiedSrcFile);
		JTypeMutator mut = source.getTopTypeWithName("TestBeanModify").asMutator();
		
		//now lets add a getter and setter
		mut.addMethod("public void setMyField(String myField ){ this.myField = myField;}");
		mut.addMethod("public String getMyField(){ return this.myField;}");
		
		//write new AST back to file
		source.asMutator().writeChangesToSrcFile();
		
		//then check it matches what we expect
		assertSourceFileAstsMatch(srcFileExpected, modifiedSrcFile);
	}
	
	private void addProperty(BeanDefinition def, Class<?> propertyType) {
		String name;
		if (propertyType.isPrimitive()) {
			name = "primitive" + ClassNameUtil.upperFirstChar(propertyType.getSimpleName());
		} else {
			name = ClassNameUtil.lowerFirstChar( propertyType.getSimpleName() );
		}
		addProperty(def, name, propertyType);
	}

	private void addProperty(BeanDefinition def, String propertyName, Class<?> propertyType) {
		def.addProperty(property(propertyName, propertyType));
	}

	private PropertyDefinition property(String name, Class<?> type) {
		PropertyDefinition p = new PropertyDefinition();
		p.setIgnore(false);
		p.setName(name);
		p.setType(type);
		return p;
	}
}
