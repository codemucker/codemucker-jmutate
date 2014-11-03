package org.codemucker.jmutate.transform;

import static org.codemucker.lang.Check.checkNotNull;

import org.codemucker.jmutate.ClashStrategy;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.PlacementStrategies;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JField.SingleJField;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.builder.JFieldBuilder;

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
public class FieldSplitterTransform implements Transform {

	@Inject
	private JMutateContext ctxt;

	private JType target;
	private JField field;
	
	public static FieldSplitterTransform newTransform() {
		return new FieldSplitterTransform();
	}

	public FieldSplitterTransform setContext(JMutateContext ctxt) {
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

	@Override
	public void transform() {
		checkNotNull("target", target);
		checkNotNull("field", field);
		checkNotNull("context", ctxt);
		
		InsertFieldTransform inserter = ctxt.obtain(InsertFieldTransform.class)
			.target(target)
			.placementStrategy(ctxt.obtain(PlacementStrategies.class).getFieldStrategy())
			.clashStrategy(ClashStrategy.REPLACE);
		
		//copy the shared field info
		JFieldBuilder fieldBuilder = ctxt.obtain(JFieldBuilder.class)
			.fieldAccess(field.getAccess())
			.fieldType(field.getType());
			
		for( SingleJField single:field.asSingleFields()){
			JField newField = fieldBuilder
				.fieldName(single.getName())
				.fieldValue(single.getInitilizer())
				.build();	
			inserter.field(newField).transform();
		}
		//remove the old fields
		field.getAstNode().delete();
	}
}
