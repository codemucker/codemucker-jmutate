package com.bertvanbrakel.codemucker.pattern;

import static com.bertvanbrakel.lang.Check.*;

import java.util.List;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.Flattener;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.JField.SingleJField;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JTypeMatchers;
import com.bertvanbrakel.codemucker.transform.InsertTypeTransform;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SetterMethodBuilder;
import com.bertvanbrakel.codemucker.transform.SetterMethodBuilder.RETURN;
import com.google.inject.Inject;

/**
 * Add or update a builder to build a bean
 * 
 * TODO:generate the 'build' method
 */
public class BeanBuilderPattern {
	
	@Inject	
	private MutationContext ctxt;
	
	private JType target;
	
	/**
	 * How do we collect the fields? Do we keep a local copy, set it on the bean directly via direct field access,
	 * or via the beans setter methods?
	 */
	static enum MODE {
		COPYOF,BEAN_FIELD,BEAN_METHOD;
	}
	
	private MODE mode = MODE.COPYOF;
	
	public void apply() {
		checkNotNull("ctxt", ctxt);
		checkNotNull("target", target);

		JType builder = getOrCreateBeanBuilder(target);
	    generateBuilderGettersAndSetters(target, builder);
	    //generateBeanCtor(target);
	    //generateBuilderBuildMethod()
	    //TODO:generate build method!
	    //Need to find ctor args..
	}

	private JType getOrCreateBeanBuilder(JType type) {
	    JType builder;
	    List<JType> builders = type.findDirectChildTypesMatching(JTypeMatchers.withSimpleName("Builder")).toList();
		if (builders.size() == 1) {
	    	builder = builders.get(0);
		} else if (builders.size() == 0) {
	    	builder = ctxt.newSourceTemplate()
	    		.pl("public static class Builder {} ")
	    		.asJType();
	    	
	    	ctxt.obtain(InsertTypeTransform.class)
	    		.setTarget(type)
	    		.setType(builder)
	    		.apply();
	    	//we want a handle to the inserted nodes. These are copied on insert so adding anything to the 
	    	//original node doesn't make it in
	    	builder = type.findDirectChildTypesMatching(JTypeMatchers.withSimpleName("Builder")).toList().get(0);
	    } else {
	    	throw new CodemuckerException("expected only a single builder on %s", type);
	    }
	    return builder;
    }
	
	private void generateBuilderGettersAndSetters(JType target, JType builder) {
	    //add the builder fields and setters
	    BeanPropertyPattern pattern = ctxt.obtain(BeanPropertyPattern.class)
	    	.setTarget(builder)
	    	.setCreateAccessor(false)
	    	.setSetterReturn(SetterMethodBuilder.RETURN.TARGET);
	    
	    for(JField f:target.findAllFields()){
	    	pattern.setPropertyType(f.getType());		
			for (SingleJField sf : f.asSingleFields()) {
				pattern.setPropertyName(sf.getName());
				pattern.apply();
			}
	    }
    }

	public BeanBuilderPattern setCtxt(MutationContext ctxt) {
    	this.ctxt = ctxt;
    	return this;
    }

	public BeanBuilderPattern setTarget(JType type) {
    	this.target = type;
    	return this;
	}    	
}