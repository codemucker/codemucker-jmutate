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
		SrcWriter src1 = new SrcWriter();
		src1.println("package com.bertvanbrakel.codegen.bean;");
		src1.println( "public class TestBeanModify {");
		src1.println( "@BeanProperty");
		src1.println( "private String myField;" );
		src1.println("}");

		SrcWriter src2 = new SrcWriter();
		src2.println("package com.bertvanbrakel.codegen.bean;");
		src2.println( "public class TestBeanModify {");
		src2.println( "private String myField;" );
		src2.println( "public void setMyField(String myField ){ this.myField = myField;}" );
		src2.println( "public String getMyField(){ return this.myField;}" );
		src2.println("}");

		File srcFile = writeNewJavaFile(src1);
		File srcFile2 = writeNewJavaFile(src2);
		
		//now lets add a getter and setter
		CompilationUnit cu = getAstFromFileWithNoErrors(srcFile);
		AST ast = cu.getAST();
		MethodDeclaration m = ast.newMethodDeclaration();
		
		//write new AST back to file
		
		//then check it matches what we expect
		assertSourceFileAstsMatch(srcFile, srcFile2);
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
