package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

import org.codemucker.jmutate.ClashStrategyResolver;
import org.codemucker.jmutate.PlacementStrategy;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import com.google.inject.Inject;

/**
 * Base helper transform to insert a set of AST nodes. Up to implementations to determine where in the AST these are inserted (handling duplicates)
 * @param <TSelf>
 */
public abstract class AbstractInsertNodeTransform<TNode,TSelf extends AbstractInsertNodeTransform<TNode,TSelf>> implements Transform {
	private JType target;
	
	@Inject
	private ClashStrategyResolver clashResolver = new ClashStrategyResolver.Fixed(ClashStrategy.ERROR);
	
	@Inject
	private PlacementStrategy placementStrategy;

	protected void checkFieldsSet(){
		checkState(target != null, "missing target");
		checkState(placementStrategy != null, "missing strategy");
		checkState(clashResolver != null, "missing clash strategy");
	}
	
	@Override
	public abstract void transform();
	
	public TSelf target(AbstractTypeDeclaration target) {
    	target(JType.from(target));
    	return self();
    }

	public TSelf target(JType javaType) {
    	this.target = javaType;
    	return self();
    }

	public TSelf clashStrategy(ClashStrategy strategy) {
    	this.clashResolver = new ClashStrategyResolver.Fixed(strategy);
    	return self();
    }

	public TSelf clashStrategy(ClashStrategyResolver resolver) {
    	this.clashResolver = resolver;
    	return self();
    }

	public TSelf placementStrategy(PlacementStrategy strategy) {
    	this.placementStrategy = strategy;
    	return self();
    }
	
	@SuppressWarnings("unchecked")
    private TSelf self(){
		return (TSelf) this;
	}

	public JType getTarget() {
    	return target;
    }

	public ClashStrategyResolver getClashStrategyResolver() {
    	return clashResolver;
    }

	public PlacementStrategy getPlacementStrategy() {
    	return placementStrategy;
    }
}
