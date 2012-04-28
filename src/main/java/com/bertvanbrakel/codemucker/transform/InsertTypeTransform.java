package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.PlacementStrategies;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JMethodMatchers;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JTypeMatchers;

public class InsertTypeTransform {
	private JType target;
	private JType type;
	private ClashStrategy clashStrategy;
	private PlacementStrategy placementStrategy;

	public static InsertTypeTransform newTransform(){
		return new InsertTypeTransform();
	}
	
	public InsertTypeTransform (){
		//setUseDefaultPlacementStrategy();
		setUseDefaultClashStrategy();
	}
	
	public void apply() {
		checkState(target != null, "missing target");
		checkState(type != null, "missing type");
		checkState(target != null, "missing target");
		checkState(clashStrategy != null, "missing clash strategy");
		checkState(placementStrategy != null, "missing placement strategy");
		
	    //TODO:detect if it exists?
		boolean insert = true;
		List<JType> found = target.findChildTypesMatching(JTypeMatchers.withName(type.getSimpleName())).toList();
		if( !found.isEmpty()){
			insert = false;
			JType existingType = found.get(0);
			switch(clashStrategy){
			case REPLACE:
				existingType.getAstNode().delete();
				insert = true;
				break;
			case IGNORE:
				break;
			case ERROR:
				throw new CodemuckerException("Existing type %s, not replacing with %s", existingType.getAstNode(), type);
			default:
				throw new CodemuckerException("Existing type %s, unsupported clash strategy %s", existingType.getAstNode(), clashStrategy);
			}
		}
		if( insert){
			new NodeInserter()
				.setNodeToInsert(type.getAstNode())
				.setTarget(target)
				.setStrategy(placementStrategy)
				.insert();
		}
    }

	public InsertTypeTransform setTarget(AbstractTypeDeclaration target) {
    	setTarget(new JType(target));
    	return this;
	}
	
	public InsertTypeTransform setTarget( JType target) {
    	this.target = target;
    	return this;
	}

	public InsertTypeTransform setType(AbstractTypeDeclaration type) {
    	setType(new JType(type));
    	return this;
	}
	
	public InsertTypeTransform setType(JType type) {
    	this.type = type;
    	return this;
	}

	public InsertTypeTransform setPlacementStrategy(PlacementStrategy strategy) {
    	this.placementStrategy = strategy;
    	return this;
    }
	
//	public InsertMethodTransform setUseDefaultPlacementStrategy() {
//    	this.placementStrategy = PlacementStrategies.STRATEGY_METHOD;
//    	return this;
//    }
	
	public InsertTypeTransform setClashStrategy(ClashStrategy clashStrategy) {
		if( clashStrategy == null ){
			setUseDefaultClashStrategy();
		} else {
			this.clashStrategy = clashStrategy;
		}
    	return this;
    }
	
	public InsertTypeTransform setUseDefaultClashStrategy() {
    	this.clashStrategy = ClashStrategy.ERROR;
    	return this;
    }
	

}
