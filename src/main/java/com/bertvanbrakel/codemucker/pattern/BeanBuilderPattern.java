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
import com.bertvanbrakel.codemucker.transform.ImportCleanerTransform;
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
 */
public class BeanBuilderPattern {

	@Inject
	private MutationContext ctxt;

	private JType target;

	/**
	 * WHat the name of the builder class will be
	 */
	private String builderClassName = "Builder";
	
	/**
	 * How do we collect the fields? Do we keep a local copy, set it on the bean directly via direct field access,
	 * or via the beans setter methods?
	 */
	static enum MODE {
		COPYOF,BEAN_FIELD,BEAN_METHOD;
	}

	private final MODE mode = MODE.COPYOF;

	private boolean useQualifiedName = true;
	
	public void apply() {
		checkNotNull("ctxt", ctxt);
		checkNotNull("target", target);

		Iterable<SingleJField> fields = collectSingleFields(findFeildsToInclude(target));
	    
		final JType builder = getOrCreateBuilderClass(target);
		
		ctxt.obtain(BeanBuilderPropertiesPattern.class)
			.setSingleFields(fields)
			.setTarget(builder)
			.apply();
		
	    ctxt.obtain(BeanBuilderFieldCtorPattern.class)
	    	.setTarget(target)
	    	.setSingleFields(fields)
	    	.setUseQualifiedName(useQualifiedName)
	    	.apply();
	    
	    ctxt.obtain(BeanBuilderBuildMethodPattern.class)
	    	.setTarget(builder)
	    	.setBean(target)
	    	.setSingleFields(fields)
	    	.apply();
	    
	    ctxt.obtain(ImportCleanerTransform.class)
	    	.setAddMissingImports(true)
	    	.setNodeToClean(target)
	    	.apply();
	}

	private static List<SingleJField> collectSingleFields(Iterable<JField> fields) {
		List<SingleJField> singles = newArrayList();
		for(JField field:fields){
			singles.addAll(field.asSingleFields());
		}
		return singles;
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
	
    private JType getOrCreateBuilderClass(final JType type) {
	    JType builder;
	    final List<JType> builders = type.findDirectChildTypesMatching(JTypeMatchers.withSimpleName(builderClassName)).toList();
		if (builders.size() == 1) {
	    	builder = builders.get(0);
		} else if (builders.size() == 0) {
	    	builder = ctxt.newSourceTemplate()
	    		.setVar("builderClassName",builderClassName)
	    		.pl("public static class ${builderClassName} {} ")
	    		.asJType();

	    	ctxt.obtain(InsertTypeTransform.class)
	    		.setTarget(type)
	    		.setType(builder)
	    		.apply();
	    	//we want a handle to the inserted nodes. These are copied on insert so adding anything to the
	    	//original node doesn't make it in. Hence we need to lookup the newly created
	    	//builder
	    	builder = type.findDirectChildTypesMatching(JTypeMatchers.withSimpleName(builderClassName)).toList().get(0);
	    } else {
	    	throw new CodemuckerException("expected only a single builder nameed '%s' on type %s", builderClassName, type);
	    }
	    return builder;
    }

	public BeanBuilderPattern setCtxt(final MutationContext ctxt) {
    	this.ctxt = ctxt;
    	return this;
    }

	public BeanBuilderPattern setTarget(final JType type) {
    	this.target = type;
    	return this;
	}
	
	public BeanBuilderPattern setUseQualifiedName(boolean useQualifiedName) {
		this.useQualifiedName = useQualifiedName;
		return this;
	}
	
	public static class BeanBuilderBuildMethodPattern {
		@Inject
		private MutationContext ctxt;
		//rename, possibly not a bean ..?
		private JType bean;
		//rename to builder?
		private JType target;
		private List<SingleJField> fields;
		
		
		public void apply() {
			checkNotNull("ctxt", ctxt);
			checkNotNull("target", target);
			checkNotNull("bean", bean);
			checkNotNull("fields", fields);

			JMethod buildMethod = createBuildMethod();
			ctxt.obtain(InsertMethodTransform.class)
				.setTarget(target)
				.setMethod(buildMethod)
				.apply();
		}

		private JMethod createBuildMethod() {
			SourceTemplate t = ctxt.newSourceTemplate();
			t.setVar("beanType", bean.getSimpleName());
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
			return buildMethod;
		}
		
		public BeanBuilderBuildMethodPattern setCtxt(MutationContext ctxt) {
			this.ctxt = ctxt;
			return this;
		}
		
		public BeanBuilderBuildMethodPattern setBean(JType bean) {
			this.bean = bean;
			return this;
		}

		public BeanBuilderBuildMethodPattern setTarget(JType target) {
			this.target = target;
			return this;
		}

		public BeanBuilderBuildMethodPattern setFields(Iterable<JField> fields) {
			List<SingleJField> singles = newArrayList();
			for(JField field:fields){
				singles.addAll(field.asSingleFields());
			}
			setSingleFields(singles);
			return this;
		}
		
		public BeanBuilderBuildMethodPattern setSingleFields(Iterable<SingleJField> fields) {
			this.fields = newArrayList(fields);
			return this;
		}
	}

	public static class BeanBuilderFieldCtorPattern {
		@Inject
		private MutationContext ctxt;
		private JType target;
		private List<SingleJField> fields;
		private Boolean useQualifiedName = true;
		
		private void apply() {
			checkNotNull("ctxt", ctxt);
			checkNotNull("target", target);
			checkNotNull("fields", fields);

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
		       // if( field.getType().isParameterizedType())
				if (useQualifiedName) {
					t.p(JavaNameUtil.getQualifiedName(field.getType()));
				} else {
					t.p(TypeUtil.toTypeSignature(field.getType()));
				}
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

		public BeanBuilderFieldCtorPattern setCtxt(MutationContext ctxt) {
			this.ctxt = ctxt;
			return this;
		}

		public BeanBuilderFieldCtorPattern setTarget(JType target) {
			this.target = target;
			return this;
		}

		public BeanBuilderFieldCtorPattern setFields(Iterable<JField> fields) {
			List<SingleJField> singles = newArrayList();
			for(JField field:fields){
				singles.addAll(field.asSingleFields());
			}
			setSingleFields(singles);
			return this;
		}
		
		public BeanBuilderFieldCtorPattern setSingleFields(Iterable<SingleJField> fields) {
			this.fields = newArrayList(fields);
			return this;
		}

		public BeanBuilderFieldCtorPattern setUseQualifiedName(Boolean useQualaifiedName) {
			this.useQualifiedName = useQualaifiedName;
			return this;
		}
		
	}

    public static class BeanBuilderPropertiesPattern {
    	
    	@Inject
    	private MutationContext ctxt;
    	private JType target;
    	private List<SingleJField> fields;
		
		public void apply() {	
			checkNotNull("ctxt", ctxt);
			checkNotNull("target", target);
			checkNotNull("fields", fields);

			//add the builder fields and setters
		    final BeanPropertyPattern pattern = ctxt.obtain(BeanPropertyPattern.class)
		    	.setTarget(target)
		    	.setCreateAccessor(false)
		    	.setSetterReturn(SetterMethodBuilder.RETURN.TARGET);
	
		    for(final SingleJField field:fields){
		    	pattern.setPropertyType(field.getType());
				pattern.setPropertyName(field.getName());
				pattern.apply();
		    }
	    }
		
		public BeanBuilderPropertiesPattern setCtxt(MutationContext ctxt) {
			this.ctxt = ctxt;
			return this;
		}

		public BeanBuilderPropertiesPattern setTarget(JType target) {
			this.target = target;
			return this;
		}

		public BeanBuilderPropertiesPattern setFields(Iterable<JField> fields) {
			List<SingleJField> singles = newArrayList();
			for(JField field:fields){
				singles.addAll(field.asSingleFields());
			}
			setSingleFields(singles);
			return this;
		}
		
		public BeanBuilderPropertiesPattern setSingleFields(Iterable<SingleJField> fields) {
			this.fields = newArrayList(fields);
			return this;
		}
	    
    }
}