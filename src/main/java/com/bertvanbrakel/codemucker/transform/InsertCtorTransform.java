package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JMethodMatchers;

public class InsertCtorTransform {
	
	private JMethod ctor;
	private JType target;
	private boolean replace;
	private PlacementStrategy strategy;

	public static InsertCtorTransform newTransform(){
		return new InsertCtorTransform();
	}
	
	public InsertCtorTransform(){
	//	setUseDefaultStrategy();
	}
	
	public void apply(){
		checkState(ctor != null, "missing constructor");
		checkState(target != null, "missing target");
		checkState(strategy != null, "missing strategy");

		boolean insert = true;

		if( !ctor.isConstructor()){
			throw new CodemuckerException("Constructor method is not a constructor. Method is %s",ctor);
			
		}
		if( !target.getSimpleName().equals(ctor.getName())){
			throw new CodemuckerException("Constructor method name should be the same as the target type. Expected name to be %s but as %s",target.getSimpleName(),ctor.getName());
		}
		
		List<JMethod> found = target.findMethodsMatching(JMethodMatchers.withMethodNamed(ctor.getName())).toList();
		
    	if( !found.isEmpty()){
    		insert = false;
    		JMethod existingCtor = found.get(0);
    		//modify it!??
    		if( replace ){
    			existingCtor.getAstNode().delete();
    			insert = true;
    		} else {
    			//throw?
    			throw new CodemuckerException("Existing constructor %s, not replacing with %s", existingCtor.getAstNode(), ctor.getAstNode());
    		}
    	}
    	if( insert){
    		new NodeInserter()
                .setTarget(target)
                .setNodeToInsert(ctor.getAstNode())
                .setStrategy(strategy)
                .insert();
    	}
	}

	public InsertCtorTransform setCtor(MethodDeclaration constructor) {
    	setCtor(new JMethod(constructor));
    	return this;
	}
	
	public InsertCtorTransform setCtor(JMethod constructor) {
    	this.ctor = constructor;
    	return this;
	}

	public InsertCtorTransform setTarget(AbstractTypeDeclaration target) {
    	setTarget(new JType(target));
    	return this;
	}
	
	public InsertCtorTransform setTarget(JType javaType) {
    	this.target = javaType;
    	return this;
	}

	public InsertCtorTransform setReplace(boolean replace) {
    	this.replace = replace;
    	return this;
    }

	public InsertCtorTransform setPlacementStrategy(PlacementStrategy strategy) {
    	this.strategy = strategy;
    	return this;
	}
//
//	public InsertCtorTransform setUseDefaultStrategy() {
//    	this.strategy = PlacementStrategies.STRATEGY_CTOR;
//    	return this;
//	}

}
