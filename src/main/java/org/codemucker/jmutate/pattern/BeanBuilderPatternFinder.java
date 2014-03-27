package org.codemucker.jmutate.pattern;

import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.transform.MutateContext;
import org.codemucker.jmutate.transform.Transform;

import com.google.inject.Inject;

public class BeanBuilderPatternFinder {

	@Inject
	private MutateContext ctxt;
	
	public SourceFilter.Builder GetFilter(){
		return SourceFilter.builder()
				//.addIncludeTypes(JTypeMatchers.withAnnotation(GenerateBuilder.class))
				//TODO:have matchers return confidences?? then finder can add that to results..
				.addInclude(AJType.with().fullName("*Builder"));
			
	}
	
	public Transform GetTransform(){
		//TODO:set requirements...
		return ctxt.obtain(BeanBuilderTransform.class);
	}
}
