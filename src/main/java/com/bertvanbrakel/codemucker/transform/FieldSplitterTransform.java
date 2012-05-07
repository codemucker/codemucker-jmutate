package com.bertvanbrakel.codemucker.transform;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JField.SingleJField;
import com.bertvanbrakel.codemucker.ast.JType;
import com.google.inject.Inject;
/**
 * Split a multi field declaration into individual ones
 */
public class FieldSplitterTransform {

	@Inject
	private MutationContext ctxt;

	private JType target;
	private JField field;
	
	public static FieldSplitterTransform newTransform() {
		return new FieldSplitterTransform();
	}

	public FieldSplitterTransform setContext(MutationContext ctxt) {
		this.ctxt = ctxt;
		return this;
	}

	
	public FieldSplitterTransform setTarget(JType target) {
		this.target = target;
		return this;
	}

	public FieldSplitterTransform setField(JField field) {
		this.field = field;
		return this;
	}

	public FieldSplitterTransform apply() {
		checkNotNull("target", target);
		checkNotNull("field", field);
		checkNotNull("context", ctxt);
		
		InsertFieldTransform inserter = ctxt.create(InsertFieldTransform.class)
			.setTarget(target)
			.setPlacementStrategy(ctxt.create(PlacementStrategies.class).getFieldStrategy())
			.setClashStrategy(ClashStrategy.REPLACE);
		
		//copy the shared field info
		FieldBuilder fieldBuilder = ctxt.create(FieldBuilder.class)
			.setAccess(field.getAccess())
			.setType(field.getType());
			
		for( SingleJField single:field.asSingleFields()){
			JField newField = fieldBuilder
				.setName(single.getName())
				.setInitializer(single.getInitilizer())
				.build();	
			inserter.setField(newField).apply();
		}
		//remove the old fields
		field.getAstNode().delete();
		
		return this;
	}
}
