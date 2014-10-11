package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

import org.codemucker.jfind.FindResult;
import org.codemucker.jmutate.MutateException;
import org.codemucker.jmutate.PlacementStrategy;
import org.codemucker.jmutate.ast.ContextNames;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.eclipse.jdt.core.dom.FieldDeclaration;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public final class InsertFieldTransform extends AbstractNodeInsertTransform<InsertFieldTransform>{

	private JField field;
	
	public static InsertFieldTransform newTransform(){
		return new InsertFieldTransform();
	}
	
	@Override
	public void transform() {
		checkFieldsSet();
		checkState(field != null,"missing field");
		
	    boolean insert = true;
		for( String fieldName:field.getNames()){
			//TODO:unwrap single field decl with multiple field (all same type/assignment)
			FindResult<JField> found = getTarget().findFieldsMatching(AJField.with().name(fieldName));
			if(!found.isEmpty()){
				insert = false;
				JField existingField = found.getFirst();
				switch(getClashStrategy()){
					case REPLACE:
						existingField.getAstNode().delete();
						insert = true;
						break;
					case IGNORE:
						break;
					case ERROR:
						throw new MutateException("Existing field %s, not replacing with %s", existingField.getAstNode(), field);
					default:
						throw new MutateException("Existing field %s, unsupported clash strategy %s", existingField.getAstNode(), getClashStrategy());
				}
			}
		}
		if( insert){
			new NodeInserter()
                .target(getTarget())
                .nodeToInsert(field.getAstNode())
                //TODO:allow to override this? want to make this a non greedy class!
                .placementStrategy(getPlacementStrategy())
                .insert();
		}
	}

	/**
	 * Used by the DI container to set the default
	 */
	@Inject
    public void injectPlacementStrategy(@Named(ContextNames.FIELD) PlacementStrategy strategy) {
	    setPlacementStrategy(strategy);
    }
	
	/**
	 * The field to transform
	 * 
	 * @param field
	 * @return
	 */
	public InsertFieldTransform setField(FieldDeclaration field) {
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
