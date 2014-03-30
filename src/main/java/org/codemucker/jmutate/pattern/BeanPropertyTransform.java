package org.codemucker.jmutate.pattern;

import static org.codemucker.lang.Check.checkNotBlank;
import static org.codemucker.lang.Check.checkNotNull;

import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.transform.MutateContext;
import org.codemucker.jmutate.transform.JFieldBuilder;
import org.codemucker.jmutate.transform.JMethodGetterBuilder;
import org.codemucker.jmutate.transform.InsertFieldTransform;
import org.codemucker.jmutate.transform.InsertMethodTransform;
import org.codemucker.jmutate.transform.JMethodSetterBuilder;
import org.codemucker.jmutate.transform.Transform;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.eclipse.jdt.core.dom.Type;

import com.google.inject.Inject;

/**
 * Generate a bean field, access and mutator (getter and setter) on a given target
 */
public class BeanPropertyTransform implements Transform {

	@Inject
	private MutateContext ctxt;

	private JType target;
	private String propertyName;
	private String propertyType;	
	private boolean createGetter = true;
	private boolean createSetter = true;
	private JMethodSetterBuilder.RETURNS setterReturns = JMethodSetterBuilder.RETURNS.VOID;
	
	@Override
	public void transform(){
		checkNotNull("ctxt", ctxt);
		checkNotNull("target", target);
		checkNotBlank("propertyName", propertyName);
		checkNotBlank("propertyType", propertyType);

		//insert field
		JField insertField = ctxt.obtain(JFieldBuilder.class)
			.fieldType(propertyType)
			.fieldName(propertyName)
			.build();
		
		ctxt.obtain(InsertFieldTransform.class)
			.target(target)
			.field(insertField)
			.transform();
	
		//create getters/setters	
		if (createSetter) {
			JMethod setter = ctxt.obtain(JMethodSetterBuilder.class)
				.target(target)
				.field(insertField)
				.methodAccess(JAccess.PUBLIC)
				.returns(setterReturns)
				.build();
			
			ctxt.obtain(InsertMethodTransform.class)
				.target(target)
				.method(setter)
				.transform();
		}
		if (createGetter) {
			JMethod getter = ctxt.obtain(JMethodGetterBuilder.class)
				.field(insertField)
				.methodAccess(JAccess.PUBLIC)
				.build();
			
			ctxt.obtain(InsertMethodTransform.class)
				.target(target)
				.method(getter)
				.transform();
		}
	}
	 
	@Inject
	public BeanPropertyTransform setCtxt(MutateContext ctxt) {
		this.ctxt = ctxt;
		return this;
	}

	public BeanPropertyTransform setTarget(JType target) {
		this.target = target;
		return this;
	}

	public BeanPropertyTransform setPropertyName(String propertyName) {
		this.propertyName = propertyName;
		return this;
	}

	public BeanPropertyTransform setPropertyType(Type propertyType) {
		String typeAsString = JavaNameUtil.resolveQualifiedName(propertyType);
		setPropertyType(typeAsString);
		return this;
	}

	public BeanPropertyTransform setPropertyType(String propertyType) {
		this.propertyType = propertyType;
		return this;
	}

	public BeanPropertyTransform setCreateGetter(boolean createAccessor) {
    	this.createGetter = createAccessor;
		return this;
	}

	public BeanPropertyTransform setCreateSetter(boolean createMutator) {
    	this.createSetter = createMutator;
		return this;
	}

	public BeanPropertyTransform setSetterReturns(JMethodSetterBuilder.RETURNS setterReturns) {
    	this.setterReturns = setterReturns;
		return this;
	}
}