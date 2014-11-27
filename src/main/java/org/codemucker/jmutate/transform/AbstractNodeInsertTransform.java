package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

import org.codemucker.jmutate.ClashStrategy;
import org.codemucker.jmutate.PlacementStrategy;
import org.codemucker.jmutate.ast.JType;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import com.google.inject.Inject;

/**
 * Base helper transform to insert a set of AST nodes. Up to implementations to determine where in the AST these are inserted (handling duplicates)
 * @param <S>
 */
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
	
	public S target(AbstractTypeDeclaration target) {
    	target(JType.from(target));
    	return self();
    }

	public S target(JType javaType) {
    	this.target = javaType;
    	return self();
    }

	public S clashStrategy(ClashStrategy strategy) {
    	this.clashStrategy = strategy;
    	return self();
    }

	public S placementStrategy(PlacementStrategy strategy) {
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
