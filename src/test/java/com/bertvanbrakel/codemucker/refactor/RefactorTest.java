package com.bertvanbrakel.codemucker.refactor;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import com.bertvanbrakel.codemucker.annotation.BeanProperty;
import com.bertvanbrakel.codemucker.ast.AstNodeProvider;
import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JAnnotatable;
import com.bertvanbrakel.codemucker.ast.JAnnotation;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.lang.matcher.AbstractNotNullMatcher;
import com.bertvanbrakel.lang.matcher.Matcher;
import com.bertvanbrakel.test.bean.ClassUtils;

public class RefactorTest {

//	@Test
//	public void test_add_getters_setters_builder(){
//		JSourceFinder finder = JSourceFinder.newBuilder()
//			.setSearchPaths(SearchPathsBuilder.newBuilder()
//				.setIncludeClassesDir(false)
//				.setIncludeTestDir(true)
//			)
//			.setFilter(FilterBuilder.newBuilder()
//				.setIncludeResource(ResourceMatchers.inPackage(TstBean.class))
//				.setIncludeTypes(JTypeMatchers.withAnnotation(GenerateBean.class))
//			)
//			.build();
//		
//		FindResult<JType> found = finder.findTypes();
//		
//		for(JType type:found){
//			assertEquals(TstBean.class.getName(), type.getFullName());			
//			//TODO:generate getters/setters
//			
//			FindResult<JField> fields = type.findAllFields();
//			//FindResult<JMethod> methods = type.findAllJavaMethods();
//			//findMethods().asMap,asCollection,... FindResults object?
//			Map<String,MethodDef> setterMap = newHashMap();
//			Map<String,MethodDef> getterMap = newHashMap();
//			
//			KeyProvider<String, JMethod> propertyNameProvider = new KeyProvider<String, JMethod>() {
//				
//				@Override
//				public String getKeyFor(JMethod method) {
//					return extractPropertyName(method);
//				}
//			};
//			
//			FindResult<JMethod> getters = type.findMethodsMatching(getterMatcher());
//			FindResult<JMethod> setters = type.findMethodsMatching(setterMatcher());
//			
//			
//			//find existing getters/setters
//			for( JMethod method:methods){
//				MethodDef def = extracteMthodDef(method);
//				if( def != null) {
//					if( def.isReader()){
//						getterMap.put(def.name, def);
//					} else if (def.isWriter()){
//						setterMap.put(def.name, def);
//					}
//				}
//			}
//			JTypeMutator typeMut = type.asMutator();
//			
//			//find getters/setters by name, _or_annotation marker....
//			for( JField field:fields){
//				//TODO:find corresponding methods
//				//TODO:determien if to ignore getter/setter for this field. Class annon, field annon?
//				Collection<String> propertyNames = extractPropertyNames(field);
//				for( String name:propertyNames){
//					MethodDef def = setterMap.get(name);
//					if( def == null){
//						 Collection<Object> args = newArrayList();
//						 args.add(ClassNameUtil.upperFirstChar(name));
//						 args.add(field.getFieldNode().getType());
//						 args.add(name);
//						 args.add(name);
//						 args.add(name);
//						 
//						 typeMut.addMethod("public void set%s(%s %s){ this.%s = %s; }",args.toArray());
//					}
//					def = getterMap.get(name);
//					if( def == null){
//						 Collection<Object> args = newArrayList();
//						 args.add(field.getFieldNode().getType());
//						 args.add(ClassNameUtil.upperFirstChar(name));
//						 args.add(name);
//						 
//						 typeMut.addMethod("public %s get%s(){ return this.%s; }",args.toArray());	
//					}	
//				}
//			}
//			
//			
//			
//			fail("TODO:implement me!");	
//		}
//		JChangeSet changes = new JChangeSet();
//		for( JType type:found){
//			type.getTypeNode().getAST()
//			changes.add(type)
//		}
//		changes.save();
//	}
	
	static class JChangeSet{
		
		public void add(AstNodeProvider provider){
			
		}
		public void save(){}
		
		
	}
	
	private Matcher<JMethod> getterMatcher(){
		return new AbstractNotNullMatcher<JMethod>(){
			@Override
            public boolean matchesSafely(JMethod found) {
	            return ClassUtils.isReaderMethodFromName(found.getName());
            }
		};
	}
	
	private Matcher<JMethod> setterMatcher(){
		return new AbstractNotNullMatcher<JMethod>(){
			@Override
            public boolean matchesSafely(JMethod found) {
	            return ClassUtils.isWriterMethodFromName(found.getName());
            }
		};
	}
	
	
	private MethodDef extracteMthodDef(JMethod m){
		if( isSetterMethod(m)){
			MethodDef def = new MethodDef();
			def.name = extractPropertyName(m);
			def.type = MethodDef.TYPE.WRITER;
			def.method = m;
			return def;
		} else if (isGetterMethod(m)){
			MethodDef def = new MethodDef();
			def.name = extractPropertyName(m);
			def.type = MethodDef.TYPE.READER;
			def.method = m;
			return def;
		} //if builder method???
		return null;
	}
	
	private Collection<String> extractPropertyNames(JField field){
		Collection<String> names= newArrayList();
		String name = getAnonationValue(field, BeanProperty.class, "name");
		if (name !=null) {
			names.add(name);
		} else {
			for(String fieldName:field.getNames()){
				name = ClassUtils.extractPropertyNameFromMethod(fieldName);
				if( name != null ){
					names.add(name);
				}
			}
		}
		return names;	
	}
	
	private String extractPropertyName(JMethod method) {
		String name = getAnonationValue(method, BeanProperty.class, "name");
		if (name == null) {
			name = ClassUtils.extractPropertyNameFromMethod(method.getName());
		}
		return name;
	}
	
	private boolean isSetterMethod(JMethod m){
		return ClassUtils.isWriterMethodFromName(m.getName());
	}

	private boolean isGetterMethod(JMethod m){
		//or a builder method???
		return ClassUtils.isReaderMethodFromName(m.getName());
	}

	private <A extends Annotation> String getAnonationValue(JAnnotatable annotatable, Class<A> anon, String attributeName){
		JAnnotation janon = annotatable.getAnnotationOfType(BeanProperty.class);
		if( anon != null ){
			String value = janon.getValueForAttribute("name", null);
			if( StringUtils.isBlank(value)){
				throw new CodemuckerException("Expected a value for 'name' for annotation 'BeanProperty'");
			}
			return value;
		} 
		return null;
	}
	
	static class MethodDef {
		static enum TYPE {READER,WRITER};
		
		String name;
		boolean ignore;
		TYPE type;
		JMethod method;
		
		boolean isWriter(){
			return type == TYPE.WRITER;
		}
		boolean isReader(){
			return type == TYPE.READER;
		}
		
	}
}

