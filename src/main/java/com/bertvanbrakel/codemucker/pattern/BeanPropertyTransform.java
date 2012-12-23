package com.bertvanbrakel.codemucker.pattern;

import static com.bertvanbrakel.lang.Check.checkNotBlank;
import static com.bertvanbrakel.lang.Check.checkNotNull;

import org.eclipse.jdt.core.dom.Type;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.transform.FieldBuilder;
import com.bertvanbrakel.codemucker.transform.GetterMethodBuilder;
import com.bertvanbrakel.codemucker.transform.InsertFieldTransform;
import com.bertvanbrakel.codemucker.transform.InsertMethodTransform;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SetterMethodBuilder;
import com.bertvanbrakel.codemucker.transform.Transform;
import com.bertvanbrakel.codemucker.util.JavaNameUtil;
import com.google.inject.Inject;

/**
 * Generate a bean field, access and mutator (getter and setter) on a given target
 */
public class BeanPropertyTransform implements Transform {

	@Inject
	private MutationContext ctxt;

	private JType target;
	private String propertyName;
	private String propertyType;	
	private boolean createAccessor = true;
	private boolean createMutator = true;
	private SetterMethodBuilder.RETURN setterReturn = SetterMethodBuilder.RETURN.VOID;
	
	@Override
	public void transform(){
		checkNotNull("ctxt", ctxt);
		checkNotNull("target", target);
		checkNotBlank("propertyName", propertyName);
		checkNotBlank("propertyType", propertyType);

		JField field = ctxt.obtain(FieldBuilder.class)
			.setFieldType(propertyType)
			.setFieldName(propertyName)
			.build();
		
		ctxt.obtain(InsertFieldTransform.class)
			.setTarget(target)
			.setField(field)
			.transform();
	
		InsertMethodTransform inserter = ctxt.obtain(InsertMethodTransform.class)
			.setTarget(target)
		;
		
		if (createMutator) {
			JMethod setter = ctxt.obtain(SetterMethodBuilder.class)
				.setTarget(target)
				.setFromField(field)
				.setMethodAccess(JAccess.PUBLIC)
				.setReturnType(setterReturn)
				.build();
			inserter
				.setMethod(setter)
				.transform();
		}
		if (createAccessor) {
			JMethod getter = ctxt.obtain(GetterMethodBuilder.class)
				.setFromField(field)
				.setMethodAccess(JAccess.PUBLIC)
				.build();
			inserter
				.setMethod(getter)
				.transform();
		}
	}
	 
	@Inject
	public BeanPropertyTransform setCtxt(MutationContext ctxt) {
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
		String typeAsString = JavaNameUtil.getQualifiedName(propertyType);
		setPropertyType(typeAsString);
		return this;
	}

	public BeanPropertyTransform setPropertyType(String propertyType) {
		this.propertyType = propertyType;
		return this;
	}

	public BeanPropertyTransform setCreateAccessor(boolean createAccessor) {
    	this.createAccessor = createAccessor;
		return this;
	}

	public BeanPropertyTransform setCreateMutator(boolean createMutator) {
    	this.createMutator = createMutator;
		return this;
	}

	public BeanPropertyTransform setSetterReturn(SetterMethodBuilder.RETURN setterReturn) {
    	this.setterReturn = setterReturn;
		return this;
	}
}