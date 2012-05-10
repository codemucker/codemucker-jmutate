package com.bertvanbrakel.codemucker.transform;

import java.util.List;

import org.junit.Test;

import com.bertvanbrakel.codemucker.annotation.GenerateBuilder;
import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JField.SingleJField;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.bertvanbrakel.codemucker.ast.finder.FilterBuilder;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchPathsBuilder;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JTypeMatchers;
import com.bertvanbrakel.codemucker.pattern.BeanPropertyPattern;

public class BuilderGeneratorTest {

	@Test
	public void test(){
		MutationContext ctxt = new SimpleMutationContext();
		
		
		FindResult<JType> found = JSourceFinder.newBuilder()
			.setSearchPaths(SearchPathsBuilder.newBuilder()
				.setIncludeClassesDir(false)
				.setIncludeTestDir(true)
			)
			.setFilter(FilterBuilder.newBuilder()
				.addIncludeTypes(JTypeMatchers.withAnnotation(GenerateBuilder.class))
			)	
			.build()
			.findTypes();
		for(JType type:found){
			JType builder;
			List<JType> builders = type.findDirectChildTypesMatching(JTypeMatchers.withName("Builder")).toList();
			if( builders.size() ==1){
				builder = builders.get(0);
			} else if( builders.size() == 0){
				builder = ctxt.newSourceTemplate()
					.pl("public static class Builder {} ")
					.asJType();
				
				ctxt.obtain(InsertTypeTransform.class)
					.setTarget(type)
					.setType(builder)
					.apply();
			} else {
				throw new CodemuckerException("expected only a single builder on %s", type);
			}
			
			for(JField f:type.findAllFields()){
				
				BeanPropertyPattern bpp = ctxt.obtain(BeanPropertyPattern.class)
					.setTarget(builder)
					.setPropertyType(f.getType());
					
				for( SingleJField sf :f.asSingleFields()){
					bpp.setPropertyName(sf.getName());
					bpp.apply();
				}
			}
			
			//now the build method?
			
			//shall we analyse ctor?
		}
		
		
	}
}
