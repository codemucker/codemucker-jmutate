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

import static com.bertvanbrakel.codemucker.util.SourceUtil.assertAstsMatch;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import com.bertvanbrakel.codemucker.annotation.BeanProperty;
import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JFieldMatchers;
import com.bertvanbrakel.codemucker.transform.GetterMethodBuilder;
import com.bertvanbrakel.codemucker.transform.InsertMethodTransform;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SetterMethodBuilder;
import com.bertvanbrakel.codemucker.transform.SetterMethodBuilder.RETURN;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.PropertyDefinition;
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
	public void test_add_setter() throws Exception {
		MutationContext ctxt = new SimpleMutationContext();
		
		SourceTemplate srcBefore = ctxt.newSourceTemplate()
			.println("package com.bertvanbrakel.codegen.bean;")
			.println("import " + BeanProperty.class.getName() + ";")
			.println( "public class TestBeanModify {")
			.println( "@BeanProperty")
			.println( "private String myField;" )
			.println("}");
		
		JType before = srcBefore.asJType();
		JType after = srcBefore.asJType();
		
		FindResult<JField> fields = before.findFieldsMatching(JFieldMatchers.withAnnotation(BeanProperty.class));
		for( JField f:fields){
			
			JMethod setter = SetterMethodBuilder.newBuilder()
    			.setAccess(JAccess.PUBLIC)
    			.setContext(ctxt)
    			.setFromField(f)
    			.setTarget(after)
    			.setMarkedGenerated(true)
    			.setReturnType(RETURN.VOID)
    			.build();
			
			JMethod getter = GetterMethodBuilder.newBuilder()
    			.setAccess(JAccess.PUBLIC)
    			.setContext(ctxt)
    			.setFromField(f)
    			.setMarkedGenerated(true)
    			.build();
			
			InsertMethodTransform inserter = InsertMethodTransform.newTransform()
				.setUseDefaultClashStrategy()
				.setTarget(after)
				.setPlacementStrategy(ctxt.getStrategies().getMethodStrategy())
				;
			
			inserter.setMethod(setter).apply();
			inserter.setMethod(getter).apply();

		}
			
		JType expectType = ctxt.newSourceTemplate()
			.println("package com.bertvanbrakel.codegen.bean;")
			.println("import " + BeanProperty.class.getName() + ";")
			.println( "public class TestBeanModify {")
			.println( "@BeanProperty")
			.println( "private String myField;" )
			.print("@").p(Pattern.class.getName()).p("(name=\"bean.setter\") public void setMyField(String myField ){ this.myField = myField;}" ).nl()
			.print("@").p(Pattern.class.getName()).p("(name=\"bean.getter\") public String getMyField(){ return this.myField;}" ).nl()
			
			//.println( "public String getMyField(){ return this.myField;}" )
			.println("}")
			.asJType();
		
		assertAstsMatch(expectType.getCompilationUnit(),after.getCompilationUnit());
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
