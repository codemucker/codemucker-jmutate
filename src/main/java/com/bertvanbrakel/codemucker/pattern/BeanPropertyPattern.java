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
import com.bertvanbrakel.codemucker.transform.SetterMethodBuilder;
import com.bertvanbrakel.codemucker.transform.SimpleFieldBuilder;
import com.bertvanbrakel.codemucker.transform.SetterMethodBuilder.RETURN;

public class BeanPropertyPattern {

	private MutationContext ctxt;
	private JType target;
	private String propertyName;
	private String propertyType;

	public BeanPropertyPattern setCtxt(MutationContext ctxt) {
		this.ctxt = ctxt;
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

		JField field = SimpleFieldBuilder.newBuilder()
			.setContext(ctxt)
			
			.setMarkedGenerated(true)
			.setType(propertyType)
			.setName(propertyName)
			.build();
				
		JMethod setter = SetterMethodBuilder.newBuilder()
			.setContext(ctxt)
			.setTarget(target)
			.setFromField(field)
			.setAccess(JAccess.PUBLIC)
			.setMarkedGenerated(true)
			.setReturnType(RETURN.VOID)
			.build();
		
		JMethod getter = GetterMethodBuilder.newBuilder()
			.setContext(ctxt)
			.setFromField(field)
			.setAccess(JAccess.PUBLIC)
			.setMarkedGenerated(true)
			.build();
		
		InsertFieldTransform.newTransform()
			.setTarget(target)
			.setField(field)
			.setPlacementStrategy(ctxt.getStrategies().getFieldStrategy())
			.apply();
		
		InsertMethodTransform inserter = InsertMethodTransform.newTransform()
			.setTarget(target)
			.setPlacementStrategy(ctxt.getStrategies().getMethodStrategy())
		;

		inserter.setMethod(setter).apply();
		inserter.setMethod(getter).apply();
	}
}