package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import com.bertvanbrakel.codemucker.ast.JType;

public abstract class AbstractNodeInsertTransform<S extends AbstractNodeInsertTransform<S>> {

	private JType target;
	private ClashStrategy clashStrategy = ClashStrategy.ERROR;
	private PlacementStrategy placementStrategy;

	protected void checkFieldsSet(){
		checkState(target != null, "missing target");
		checkState(placementStrategy != null, "missing strategy");
		checkState(clashStrategy != null, "missing clash strategy");
	}
	
	public S setTarget(AbstractTypeDeclaration target) {
    	setTarget(new JType(target));
    	return self();
    }

	public S setTarget(JType javaType) {
    	this.target = javaType;
    	return self();
    }

	public S setClashStrategy(ClashStrategy strategy) {
    	this.clashStrategy = strategy;
    	return self();
    }

	public S setPlacementStrategy(PlacementStrategy strategy) {
    	this.placementStrategy = strategy;
    	return self();
    }
	
	@SuppressWarnings("unchecked")
    private S self(){
		return (S) this;
	}

	public JType getTarget() {
    	return target;
    }

	public ClashStrategy getClashStrategy() {
    	return clashStrategy;
    }

	public PlacementStrategy getPlacementStrategy() {
    	return placementStrategy;
    }
}
