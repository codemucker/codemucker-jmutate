package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

import org.codemucker.jmutate.ast.JType;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import com.google.inject.Inject;

public abstract class AbstractNodeInsertTransform<S extends AbstractNodeInsertTransform<S>> implements Transform {
	private JType target;
	
	@Inject
	private ClashStrategy clashStrategy = ClashStrategy.ERROR;
	//@Inject
	private PlacementStrategy placementStrategy;

	protected void checkFieldsSet(){
		checkState(target != null, "missing target");
		checkState(placementStrategy != null, "missing strategy");
		checkState(clashStrategy != null, "missing clash strategy");
	}
	
	@Override
	public abstract void transform();
	
	public S setTarget(AbstractTypeDeclaration target) {
    	target(JType.from(target));
    	return self();
    }

	public S target(JType javaType) {
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
