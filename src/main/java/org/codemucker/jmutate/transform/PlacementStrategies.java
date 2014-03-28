package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import org.codemucker.jmutate.ast.StrategyBeforeAfterNodes;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class PlacementStrategies {

	private final PlacementStrategy fieldStrategy;
	private final PlacementStrategy methodStrategy;
	private final PlacementStrategy ctorStrategy;
	private final PlacementStrategy typeStrategy;
	
	public static Builder with(){
		return new Builder();
	}
	
	private PlacementStrategies(
			PlacementStrategy fieldStrategy
			, PlacementStrategy ctorStrategy
            , PlacementStrategy methodStrategy
			, PlacementStrategy typeStrategy) {
	    super();
	    this.fieldStrategy = checkNotNull(fieldStrategy,"expect field strategy");
	    this.methodStrategy = checkNotNull(methodStrategy,"expect method strategy");
	    this.ctorStrategy = checkNotNull(ctorStrategy,"expect ctor strategy");
	    this.typeStrategy = checkNotNull(typeStrategy,"expect class strategy");
    }

	public PlacementStrategy getFieldStrategy(){
		return fieldStrategy;
	}
	
	public PlacementStrategy getMethodStrategy(){
		return methodStrategy;
	}
	
	public PlacementStrategy getCtorStrategy(){
		return ctorStrategy;
	}
	
	public PlacementStrategy getTypeStrategy(){
		return typeStrategy;
	}

	public static class Builder {

		private static final PlacementStrategy DEFAULT_STRATEGY_FIELD = StrategyBeforeAfterNodes.with()
			.afterNodes(FieldDeclaration.class)
			.beforeNodes(MethodDeclaration.class, TypeDeclaration.class, EnumDeclaration.class)
			.build();	
		private static final PlacementStrategy DEFAULT_STRATEGY_METHOD = StrategyBeforeAfterNodes.with()
			.afterNodes(MethodDeclaration.class, FieldDeclaration.class, EnumDeclaration.class)
		    .beforeNodes(TypeDeclaration.class)
		    .build();
		private static final PlacementStrategy DEFAULT_STRATEGY_CTOR = StrategyBeforeAfterNodes.with()
			.afterNodes(FieldDeclaration.class, EnumDeclaration.class)
			.beforeNodes(TypeDeclaration.class)
			.build();
		private static final PlacementStrategy DEFAULT_STRATEGY_CLASS = StrategyBeforeAfterNodes.with()
			.afterNodes(FieldDeclaration.class, MethodDeclaration.class, EnumDeclaration.class, TypeDeclaration.class)
			.build();

		private PlacementStrategy fieldStrategy;
		private PlacementStrategy methodStrategy;
		private PlacementStrategy ctorStrategy;
		private PlacementStrategy typeStrategy;

		private Builder(){
		}		

		public PlacementStrategies build(){
			return new PlacementStrategies(fieldStrategy,ctorStrategy,methodStrategy,typeStrategy);
		}

		public Builder defaults(){
			defaultClassStrategy();
			defaultCtorStrategy();
			defaultFieldStrategy();
			defaultMethodStrategy();	

			return this;
		}

		public Builder defaultFieldStrategy() {
			fieldStrategy(DEFAULT_STRATEGY_FIELD);
			return this;
		}
		
		public Builder fieldStrategy(PlacementStrategy fieldStrategy) {
			this.fieldStrategy = fieldStrategy;
			return this;
		}

		public Builder defaultMethodStrategy() {
			methodStrategy(DEFAULT_STRATEGY_METHOD);
			return this;
		}

		public Builder methodStrategy(PlacementStrategy methodStrategy) {
			this.methodStrategy = methodStrategy;
			return this;
		}

		public Builder defaultCtorStrategy() {
			ctorStrategy(DEFAULT_STRATEGY_CTOR);
			return this;
		}

		public Builder ctorStrategy(PlacementStrategy ctorStrategy) {
			this.ctorStrategy = ctorStrategy;
			return this;
		}

		public Builder defaultClassStrategy() {
			typeStrategy(DEFAULT_STRATEGY_CLASS);
			return this;
		}

		public Builder typeStrategy(PlacementStrategy typeStrategy) {
			this.typeStrategy = typeStrategy;
			return this;
		}
	}
}
