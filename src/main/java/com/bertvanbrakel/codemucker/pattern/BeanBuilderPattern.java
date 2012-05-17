package com.bertvanbrakel.codemucker.pattern;

import static com.bertvanbrakel.lang.Check.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JField.SingleJField;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JModifiers;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JTypeMatchers;
import com.bertvanbrakel.codemucker.transform.InsertCtorTransform;
import com.bertvanbrakel.codemucker.transform.InsertMethodTransform;
import com.bertvanbrakel.codemucker.transform.InsertTypeTransform;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SetterMethodBuilder;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.codemucker.util.JavaNameUtil;
import com.bertvanbrakel.codemucker.util.TypeUtil;
import com.bertvanbrakel.test.finder.matcher.Matcher;
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

	private final MODE mode = MODE.COPYOF;

	public void apply() {
		checkNotNull("ctxt", ctxt);
		checkNotNull("target", target);

		final JType builder = getOrCreateBeanBuilder(target);
	    generateBuilderGettersAndSetters(target, builder);
	    
	    Iterable<SingleJField> fields = collectSingleFields(findFeildsToInclude(target));
	    
	    ctxt.obtain(BeanFieldCtorPattern.class)
	    	.setTarget(target)
	    	.setSingleFields(fields)
	    	.apply();
	    
	    
	    generateBuilderBuildMethod(target,builder, fields);
	    //TODO:generate build method!
	    //Need to find ctor args..
	}

	private static List<SingleJField> collectSingleFields(Iterable<JField> fields) {
		List<SingleJField> singles = newArrayList();
		for(JField field:fields){
			singles.addAll(field.asSingleFields());
		}
		return singles;
	}
	
	private void generateBuilderBuildMethod(final JType target,JType builder, final Iterable<SingleJField> fields) {
		SourceTemplate t = ctxt.newSourceTemplate();
		t.setVar("beanType", target.getSimpleName());
		t.p("public ${beanType} build(){");
		t.p("return new ${beanType}(");
		boolean comma = false;
		//TODO:use single fields!
		for (SingleJField f : fields) {
			if (comma) {
				t.p(",");
			}
			comma = true;
			t.p(f.getName());
		}
		t.p(");");
		t.p("}");
		
		JMethod buildMethod = t.asJMethod();
		ctxt.obtain(InsertMethodTransform.class)
			.setTarget(builder)
			.setMethod(buildMethod)
			.apply();
	}

	private static class BeanFieldCtorPattern {
		@Inject
		private MutationContext ctxt;
		private JType target;
		private List<SingleJField> fields;
		
		private void apply() {
		   final MethodDeclaration ctor = createCtorFromFields();
	
		    ctxt.obtain(InsertCtorTransform.class)
		        .setTarget(target)
		        .setCtor(ctor)
		        .apply();
		}
	
	    private MethodDeclaration createCtorFromFields() {
	        final SourceTemplate t = ctxt.newSourceTemplate();
	
		    t.setVar("ctorName", target.getSimpleName());
	
		    t.pl("public ${ctorName}(");
		    boolean comma = false;
		    
		    for(final SingleJField field:fields){
		    	if( comma ){
		            t.pl(",");
		        }
		        comma = true;
		       // t.p(TypeUtil.toTypeSignature(field.getType()));
		        t.p(JavaNameUtil.getQualifiedName(field.getType()));
		        t.p(" ");
		        t.p(field.getName());
		    }
		    t.pl(") {");
	
		    for(final SingleJField field:fields){
	            t.p("this.").p(field.getName()).p("=").p(field.getName()).pl(";");
	        }
	
		    t.pl("}");

		    final MethodDeclaration ctor = t.asConstructor();
	        return ctor;
	    }

		public BeanFieldCtorPattern setCtxt(MutationContext ctxt) {
			this.ctxt = ctxt;
			return this;
		}

		public BeanFieldCtorPattern setTarget(JType target) {
			this.target = target;
			return this;
		}

		public BeanFieldCtorPattern setFields(Iterable<JField> fields) {
			List<SingleJField> singles = newArrayList();
			for(JField field:fields){
				singles.addAll(field.asSingleFields());
			}
			setSingleFields(singles);
			return this;
		}
		
		public BeanFieldCtorPattern setSingleFields(Iterable<SingleJField> fields) {
			this.fields = newArrayList(fields);
			return this;
		}
	    
	}

    private JType getOrCreateBeanBuilder(final JType type) {
	    JType builder;
	    final List<JType> builders = type.findDirectChildTypesMatching(JTypeMatchers.withSimpleName("Builder")).toList();
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

	private void generateBuilderGettersAndSetters(final JType target, final JType builder) {
	    //add the builder fields and setters
	    final BeanPropertyPattern pattern = ctxt.obtain(BeanPropertyPattern.class)
	    	.setTarget(builder)
	    	.setCreateAccessor(false)
	    	.setSetterReturn(SetterMethodBuilder.RETURN.TARGET);

	    for(final JField f:findFeildsToInclude(target)){
	    	pattern.setPropertyType(f.getType());
			for (final SingleJField sf : f.asSingleFields()) {
				pattern.setPropertyName(sf.getName());
				pattern.apply();
			}
	    }
    }

	private Iterable<JField> findFeildsToInclude(final JType target){
	    return target.findFieldsMatching(new Matcher<JField>() {
            @Override
            public boolean matches(final JField field) {
                final JModifiers mods = field.getJavaModifiers();
                if( mods.isFinal() || mods.isStatic() || mods.isStrictFp()){
                    return false;
                }
                //TODO:detect annotations. Depending on mode: all by default, or need explicit
                return true;
            }
        });
	}

	public BeanBuilderPattern setCtxt(final MutationContext ctxt) {
    	this.ctxt = ctxt;
    	return this;
    }

	public BeanBuilderPattern setTarget(final JType type) {
    	this.target = type;
    	return this;
	}
}