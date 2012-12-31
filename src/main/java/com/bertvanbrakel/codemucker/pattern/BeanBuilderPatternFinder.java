package com.bertvanbrakel.codemucker.pattern;

import com.bertvanbrakel.codemucker.ast.finder.Filter;
import com.bertvanbrakel.codemucker.ast.matcher.AType;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.Transform;
import com.google.inject.Inject;

public class BeanBuilderPatternFinder {

	@Inject
	private MutationContext ctxt;
	
	public Filter.Builder GetFilter(){
		return Filter.newBuilder()
				//.addIncludeTypes(JTypeMatchers.withAnnotation(GenerateBuilder.class))
				//TODO:have matchers return confidences?? then finder can add that to results..
				.addIncludeTypes(AType.withFullName("*Builder"));
			
	}
	
	public Transform GetTransform(){
		//TODO:set requirements...
		return ctxt.obtain(BeanBuilderTransform.class);
	}
}
