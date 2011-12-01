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
import static com.bertvanbrakel.codemucker.util.SourceUtil.getAstFromClassBody;
import static com.bertvanbrakel.codemucker.util.SourceUtil.getAstFromFileWithNoErrors;
import static com.bertvanbrakel.codemucker.util.SourceUtil.writeNewJavaFile;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

import com.bertvanbrakel.codemucker.bean.BeanBuilderGenerator;
import com.bertvanbrakel.codemucker.bean.GeneratorOptions;
import com.bertvanbrakel.codemucker.util.SrcWriter;
import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.PropertyDefinition;
import com.bertvanbrakel.test.util.ClassNameUtil;
import com.bertvanbrakel.test.util.ProjectFinder;

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

		File modifiedSrcFile = writeNewJavaFile(srcBefore);
		File srcFileExpected = writeNewJavaFile(srcExpected);
		
		//now lets add a getter and setter
		CompilationUnit cu = getAstFromFileWithNoErrors(modifiedSrcFile);
		AST ast = cu.getAST();
	//	MethodDeclaration m = ast.newMethodDeclaration();
		
		//write new AST back to file
		
		//then check it matches what we expect
		assertSourceFileAstsMatch(srcFileExpected, modifiedSrcFile);
	}
	

	@Test
	public void test_eclipse_find() throws Exception {
		File srcDir = ProjectFinder.findDefaultMavenCompileDir();
		File srcFile = new File(srcDir, "com/bertvanbrakel/test/finder/ClassFinder.java");

//		
//        final List<SearchMatch> references = new ArrayList<SearchMatch>();
//        
//        SearchPattern pattern = SearchPattern.createPattern("setShort", 
//                                                            IJavaSearchConstants.ALL_OCCURRENCES,
//                                                            IJavaSearchConstants.REFERENCES,
//                                                            SearchPattern.R_FULL_MATCH);
//        if (pattern == null) {
//            // E.g. element not found / no longer exists
//            throw new NullPointerException("No pattern!?");
//        }
//        
//        SearchRequestor requestor = new SearchRequestor() {
//            @Override public void acceptSearchMatch(SearchMatch match) throws CoreException {
//                references.add(match);
//            }
//        };
//        
//        IJavaSearchScope scope = mm.createWorkspaceScope();
//        new SearchEngine().search(pattern,
//                                  new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
//                                  scope,
//                                  requestor,
//                                  null);
        
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
