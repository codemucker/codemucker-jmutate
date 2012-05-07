package com.bertvanbrakel.codemucker.pattern;

import static com.bertvanbrakel.lang.Check.checkNotBlank;
import static com.bertvanbrakel.lang.Check.checkNotNull;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.transform.GetterMethodBuilder;
import com.bertvanbrakel.codemucker.transform.InsertFieldTransform;
import com.bertvanbrakel.codemucker.transform.InsertMethodTransform;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.PlacementStrategies;
import com.bertvanbrakel.codemucker.transform.SetterMethodBuilder;
import com.bertvanbrakel.codemucker.transform.FieldBuilder;
import com.bertvanbrakel.codemucker.transform.SetterMethodBuilder.RETURN;
import com.google.inject.Inject;

public class BeanPropertyPattern {

	@Inject
	private MutationContext ctxt;

	@Inject
	private PlacementStrategies strategies;

	private JType target;
	private String propertyName;
	private String propertyType;	

	@Inject
	public BeanPropertyPattern setCtxt(MutationContext ctxt) {
		this.ctxt = ctxt;
		return this;
	}

	@Inject
	public BeanPropertyPattern setPlacementStrategies(PlacementStrategies strategies) {
		this.strategies = strategies;
		return this;
	}

	public BeanPropertyPattern setTarget(JType target) {
		this.target = target;
		return this;
	}

	public BeanPropertyPattern setPropertyName(String propertyName) {
		this.propertyName = propertyName;
		return this;
	}

	public BeanPropertyPattern setPropertyType(String propertyType) {
		this.propertyType = propertyType;
		return this;
	}

	public void apply(){
		checkNotNull("ctxt", ctxt);
		checkNotNull("target", target);
		checkNotBlank("propertyName", propertyName);
		checkNotBlank("propertyType", propertyType);

		JField field = ctxt.create(FieldBuilder.class)
			.setMarkedGenerated(true)
			.setType(propertyType)
			.setName(propertyName)
			.build();
				
		JMethod setter = ctxt.create(SetterMethodBuilder.class)
			.setTarget(target)
			.setFromField(field)
			.setAccess(JAccess.PUBLIC)
			.setMarkedGenerated(true)
			.setReturnType(RETURN.VOID)
			.build();
		
		JMethod getter = ctxt.create(GetterMethodBuilder.class)
			.setFromField(field)
			.setAccess(JAccess.PUBLIC)
			.setMarkedGenerated(true)
			.build();
		
		ctxt.create(InsertFieldTransform.class)
			.setTarget(target)
			.setField(field)
			.setPlacementStrategy(strategies.getFieldStrategy())
			.apply();
		
		InsertMethodTransform inserter = ctxt.create(InsertMethodTransform.class)
			.setTarget(target)
			.setPlacementStrategy(strategies.getMethodStrategy())
		;

		inserter.setMethod(setter).apply();
		inserter.setMethod(getter).apply();
	}
}