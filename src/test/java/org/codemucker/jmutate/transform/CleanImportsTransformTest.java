package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

import org.apache.commons.io.IOUtils;
import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.util.SourceAsserts;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;


public class CleanImportsTransformTest {

	JMutateContext ctxt = DefaultMutateContext.with().defaults().build();
	
	@Test
	public void test_add_imports(){	
		testTransform("add_imports");
	}

	@Test
	public void test_existing_imports_only(){
		CleanImportsTransform transform = ctxt.obtain(CleanImportsTransform.class).addMissingImports(false);	
		testTransform("existing_imports_only",transform);
	}

	@Test
	public void bug_import_full_name_for_static_inner_classes(){
		testTransform("inner_class_imports");
	}
	
	@Test
	public void bug_clean_annotations(){	
		testTransform("annotation_imports");
	}
	
	@Test
	public void qualified_fieldtype(){
		testTransform("qualified_fieldtype");
	}
	
	@Test
	public void array_types(){
		testTransform("array_types");
	}
	
	@Test
	public void ignore_if_local_types_clash(){
		testTransform("ignore_if_local_types_clash");
	}
	
	@Test
	public void ignore_if_inner_types_clash(){
		testTransform("ignore_if_inner_types_clash");
	}
	
	@Test
	public void nested_classes(){
		testTransform("nested_classes");
	}
	
	private void testTransform(String filename){
		CleanImportsTransform transform = ctxt.obtain(CleanImportsTransform.class);		
		testTransform(filename,transform);
	}
	
	private void testTransform(String filename, CleanImportsTransform transform){
		CompilationUnit actual = readTemplate(filename + ".before").asResolvedCompilationUnitNamed(null);
		CompilationUnit expected = readTemplate(filename + ".after").asResolvedCompilationUnitNamed(null);
		
		//do the actual import clean
		transform
			.nodeToClean(actual)
			.transform();
		
		SourceAsserts.assertAstsMatch(expected, actual);
	}
	
//	@Test
//	public  void genCode(){
//		System.out.println("runnign code gen");
//		ClassScanner scanner = ClassScanner.with()
//				.scanRoots(Roots.with().all().build())
//				.filter(ClassFilter.where()
//						.resourceMatches(ARootResource.with().packageName("org.eclipse.jdt.core.dom"))
//						.classMatches(AClass.that().isASubclassOf(ASTNode.class).isNotAbstract()))
//				.build();
//		List<Class<?>> classes = scanner.findClasses().toList();
//		Collections.sort(classes, new ClassNameSorter());
//		for(Class<?> c:classes){
//			System.out.println("else if( node instanceof " + c.getName() + ") { return visit(("+ c.getName()+")node);}");
//		}
//	}
	
	static class ClassNameSorter implements Comparator<Class<?>> {

		@Override
		public int compare(Class<?> left, Class<?> right) {
			return left.getSimpleName().compareTo(right.getSimpleName());
		}

		
	}
	
	private SourceTemplate readTemplate(String template){
		return ctxt.newSourceTemplate().p(readTemplateAsString(template));
	}
	
	private String readTemplateAsString(String template){
		String path = getClass().getSimpleName() + ".java." + template;
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream(path);
			checkNotNull(is,"couldn't find template:" + path);
			return IOUtils.toString(is,"UTF-8");
		} catch (IOException e){
			throw new JMutateException("error reading template path:" + path,e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}
