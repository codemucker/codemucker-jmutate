package org.codemucker.jmutate.pattern;

import org.codemucker.jmutate.ast.finder.Filter;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.transform.CodeMuckContext;
import org.codemucker.jmutate.transform.Transform;

import com.google.inject.Inject;

public class BeanBuilderPatternFinder {

	@Inject
	private CodeMuckContext ctxt;
	
	public Filter.Builder GetFilter(){
		return Filter.builder()
				//.addIncludeTypes(JTypeMatchers.withAnnotation(GenerateBuilder.class))
				//TODO:have matchers return confidences?? then finder can add that to results..
				.addIncludeTypes(AJType.withFullName("*Builder"));
			
	}
	
	public Transform GetTransform(){
		//TODO:set requirements...
		return ctxt.obtain(BeanBuilderTransform.class);
	}
}
