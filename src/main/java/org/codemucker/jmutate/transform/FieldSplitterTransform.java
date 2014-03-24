package org.codemucker.jmutate.transform;

import static org.codemucker.lang.Check.checkNotNull;

import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JField.SingleJField;
import org.codemucker.jmutate.ast.JType;

import com.google.inject.Inject;
/**
 * Split a multi field declaration into individual ones. E.g.
 * 
 * <pre>string a,b,c = "foo":</pre>
 * 
 * would be converted to 
 * 
 * <pre>
 * string a = "";
 * </pre>
 */
public class FieldSplitterTransform {

	@Inject
	private MutateContext ctxt;

	private JType target;
	private JField field;
	
	public static FieldSplitterTransform newTransform() {
		return new FieldSplitterTransform();
	}

	public FieldSplitterTransform setContext(MutateContext ctxt) {
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
		
		InsertFieldTransform inserter = ctxt.obtain(InsertFieldTransform.class)
			.setTarget(target)
			.setPlacementStrategy(ctxt.obtain(PlacementStrategies.class).getFieldStrategy())
			.setClashStrategy(ClashStrategy.REPLACE);
		
		//copy the shared field info
		FieldBuilder fieldBuilder = ctxt.obtain(FieldBuilder.class)
			.setFieldAccess(field.getAccess())
			.setFieldType(field.getType());
			
		for( SingleJField single:field.asSingleFields()){
			JField newField = fieldBuilder
				.setFieldName(single.getName())
				.setFieldInitializer(single.getInitilizer())
				.build();	
			inserter.setField(newField).transform();
		}
		//remove the old fields
		field.getAstNode().delete();
		
		return this;
	}
}
