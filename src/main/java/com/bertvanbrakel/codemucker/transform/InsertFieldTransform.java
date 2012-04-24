package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.PlacementStrategies;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JFieldMatchers;

public class InsertFieldTransform {

	private JType target;
	private JField field;
	private ClashStrategy clashStrategy = ClashStrategy.ERROR;

	private PlacementStrategy placementStrategy;
	
	public static InsertFieldTransform newTransform(){
		return new InsertFieldTransform();
	}
	
	public InsertFieldTransform(){
		setUseDefaultPlacementStrategy();
		setUseDefaultClashStrategy();
	}
	
	public void apply() {
		checkState(target != null,"missing target");
		checkState(field != null,"missing field");
		checkState(placementStrategy != null,"missing strategy");
		
	    boolean insert = true;
		for( String fieldName:field.getNames()){
			List<JField> found = target.findFieldsMatching(JFieldMatchers.withName(fieldName)).toList();
			if( !found.isEmpty()){
				insert = false;
				JField existingField = found.get(0);
				switch(clashStrategy){
				case REPLACE:
					existingField.getAstNode().delete();
					insert = true;
					break;
				case IGNORE:
					break;
				case ERROR:
					throw new CodemuckerException("Existing field %s, not replacing with %s", existingField.getAstNode(), field);
				default:
					throw new CodemuckerException("Existing field %s, unsupported clash strategy %s", existingField.getAstNode(), clashStrategy);
				}
			}
		}
		if( insert){
			new NodeInserter()
                .setTarget(target)
                .setNodeToInsert(field.getAstNode())
                //TODO:allow to override this? want to make this a non greedy class!
                .setStrategy(placementStrategy)
                .insert();
		}
    }

	public InsertFieldTransform setTarget(AbstractTypeDeclaration target) {
    	setTarget(new JType(target));
    	return this;
	}
	
	public InsertFieldTransform setTarget(JType target) {
    	this.target = target;
    	return this;
	}

	public InsertFieldTransform setField(FieldDeclaration field) {
    	setField(new JField(field));
    	return this;
	}
	
	public InsertFieldTransform setField(JField field) {
    	this.field = field;
    	return this;
	}

	public InsertFieldTransform setPlacementStrategy(PlacementStrategy strategy) {
    	if( strategy == null){
    		setUseDefaultPlacementStrategy();
    	} else {
    		this.placementStrategy = strategy;
    	}
    	return this;
	}
	
	public InsertFieldTransform setUseDefaultPlacementStrategy() {
    	this.placementStrategy = PlacementStrategies.STRATEGY_FIELD;
    	return this;
	}
	
	public InsertFieldTransform setClashStrategy(ClashStrategy clashStrategy) {
		if (clashStrategy == null) {
			setUseDefaultClashStrategy();
		} else {
			this.clashStrategy = clashStrategy;
		}
		return this;
	}
	
	public InsertFieldTransform setUseDefaultClashStrategy() {
		this.clashStrategy = ClashStrategy.ERROR;
    	return this;
	}
}
