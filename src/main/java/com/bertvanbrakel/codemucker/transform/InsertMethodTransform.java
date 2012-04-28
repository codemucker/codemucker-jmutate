package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JMethodMatchers;

public class InsertMethodTransform {
	private JType target;
	private JMethod method;
	private ClashStrategy clashStrategy;
	private PlacementStrategy placementStrategy;


	public static InsertMethodTransform newTransform(){
		return new InsertMethodTransform();
	}
	
	public InsertMethodTransform (){
		//setUseDefaultPlacementStrategy();
		setUseDefaultClashStrategy();
	}
	
	public void apply() {
		checkState(target != null, "missing target");
		checkState(method != null, "missing method");
		checkState(clashStrategy != null, "missing clash strategy");
		checkState(placementStrategy != null, "missing placement strategy");
		
	    //TODO:detect if it exists?
		boolean insert = true;
		List<JMethod> found = target.findMethodsMatching(JMethodMatchers.withNameAndArgSignature(method)).toList();
		if( !found.isEmpty()){
			insert = false;
			JMethod existingMethod = found.get(0);
			switch(clashStrategy){
			case REPLACE:
				existingMethod.getAstNode().delete();
				insert = true;
				break;
			case IGNORE:
				break;
			case ERROR:
				throw new CodemuckerException("Existing method %s, not replacing with %s", existingMethod.getAstNode(), method);
			default:
				throw new CodemuckerException("Existing method %s, unsupported clash strategy %s", existingMethod.getAstNode(), clashStrategy);
			}
		}
		if( insert){
			new NodeInserter()
				.setNodeToInsert(method.getAstNode())
				.setTarget(target)
				.setStrategy(placementStrategy)
				.insert();
		
		}
    }

	public InsertMethodTransform setTarget(AbstractTypeDeclaration target) {
    	setTarget(new JType(target));
    	return this;
	}
	
	public InsertMethodTransform setTarget( JType target) {
    	this.target = target;
    	return this;
	}

	public InsertMethodTransform setMethod(MethodDeclaration method) {
    	setMethod(new JMethod(method));
    	return this;
	}
	
	public InsertMethodTransform setMethod(JMethod newMethod) {
    	this.method = newMethod;
    	return this;
	}

	public InsertMethodTransform setPlacementStrategy(PlacementStrategy strategy) {
    	this.placementStrategy = strategy;
    	return this;
    }
	
//	public InsertMethodTransform setUseDefaultPlacementStrategy() {
//    	this.placementStrategy = PlacementStrategies.STRATEGY_METHOD;
//    	return this;
//    }
	
	public InsertMethodTransform setClashStrategy(ClashStrategy clashStrategy) {
		if( clashStrategy == null ){
			setUseDefaultClashStrategy();
		} else {
			this.clashStrategy = clashStrategy;
		}
    	return this;
    }
	
	public InsertMethodTransform setUseDefaultClashStrategy() {
    	this.clashStrategy = ClashStrategy.ERROR;
    	return this;
    }
	

}
