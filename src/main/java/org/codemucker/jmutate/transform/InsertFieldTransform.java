package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

import org.codemucker.jfind.FindResult;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.PlacementStrategy;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;

import com.google.inject.Inject;

public final class InsertFieldTransform extends AbstractInsertNodeTransform<FieldDeclaration,InsertFieldTransform>{

	private JField field;
	
	public static InsertFieldTransform newTransform(){
		return new InsertFieldTransform();
	}
	
	@Override
	public void transform() {
		checkFieldsSet();
		checkState(field != null,"missing field");
		
		for( String fieldName:field.getNames()){
			//TODO:unwrap single field decl with multiple field (all same type/assignment)
			FindResult<JField> found = getTarget().findFieldsMatching(AJField.with().name(fieldName));
			if(!found.isEmpty()){
				JField existingField = found.getFirst();
				switch(getClashStrategyResolver().resolveClash(existingField.getAstNode(), field.getAstNode())){
					case REPLACE:
						insert(field.getAstNode(), existingField.getAstNode());
						existingField.getAstNode().delete();
						return;
					case SKIP:
						return;
					case ERROR:
						throw new JMutateException("Existing field %s, not replacing with %s", existingField.getAstNode(), field);
					default:
						throw new JMutateException("Existing field %s, unsupported clash strategy %s", existingField.getAstNode(), getClashStrategyResolver());
				}
			}
		}
		insert(field.getAstNode(),null);
	}
	
	private void insert(FieldDeclaration field, ASTNode beforeNode){
		PlacementStrategy placement = new PlacementStrategySameLocation(getPlacementStrategy(),beforeNode);
		
		new InsertNodeTransform()
			.nodeToInsert(field)
			.target(getTarget())
			.placementStrategy(placement)
			.insert();
	}


	/**
	 * Used by the DI container to set the default
	 */
	@Inject
    public void injectPlacementStrategy(PlacementStrategy strategy) {
	    placementStrategy(strategy);
    }
	
	/**
	 * The field to transform
	 * 
	 * @param field
	 * @return
	 */
	public InsertFieldTransform field(FieldDeclaration field) {
    	field(JField.from(field));
    	return this;
	}
	
	/**
	 * The field to transform
	 * @param field
	 * @return
	 */
	public InsertFieldTransform field(JField field) {
    	this.field = field;
    	return this;
	}
}
