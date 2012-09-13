package com.bertvanbrakel.codemucker.pattern;

import com.bertvanbrakel.codemucker.ast.finder.FilterBuilder;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JTypeMatchers;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.Transform;
import com.google.inject.Inject;

public class BeanBuilderPatternFinder {

	@Inject
	private MutationContext ctxt;
	
	public FilterBuilder GetFilter(){
		return FilterBuilder.newBuilder()
				//.addIncludeTypes(JTypeMatchers.withAnnotation(GenerateBuilder.class))
				//TODO:have matchers return confidences?? then finder can add that to results..
				.addIncludeTypes(JTypeMatchers.withFullName("*Builder"));
			
	}
	
	public Transform GetTransform(){
		//TODO:set requirements...
		return ctxt.obtain(BeanBuilderTransform.class);
	}
}
