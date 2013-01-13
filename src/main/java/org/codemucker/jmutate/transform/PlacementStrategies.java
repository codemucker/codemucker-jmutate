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
	
	public static Builder builder(){
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

		private static final PlacementStrategy DEFAULT_STRATEGY_FIELD = StrategyBeforeAfterNodes.builder()
			.afterNodes(FieldDeclaration.class)
			.beforeNodes(MethodDeclaration.class, TypeDeclaration.class, EnumDeclaration.class)
			.build();	
		private static final PlacementStrategy DEFAULT_STRATEGY_METHOD = StrategyBeforeAfterNodes.builder()
			.afterNodes(MethodDeclaration.class, FieldDeclaration.class, EnumDeclaration.class)
		    .beforeNodes(TypeDeclaration.class)
		    .build();
		private static final PlacementStrategy DEFAULT_STRATEGY_CTOR = StrategyBeforeAfterNodes.builder()
			.afterNodes(FieldDeclaration.class, EnumDeclaration.class)
			.beforeNodes(TypeDeclaration.class)
			.build();
		private static final PlacementStrategy DEFAULT_STRATEGY_CLASS = StrategyBeforeAfterNodes.builder()
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

		public Builder setDefaults(){
			useDefaultClassStrategy();
			useDefaultCtorStrategy();
			useDefaultFieldStrategy();
			useDefaultMethodStrategy();	

			return this;
		}

		public Builder useDefaultFieldStrategy() {
			fieldStrategy(DEFAULT_STRATEGY_FIELD);
			return this;
		}
		
		public Builder fieldStrategy(PlacementStrategy fieldStrategy) {
			this.fieldStrategy = fieldStrategy;
			return this;
		}

		public Builder useDefaultMethodStrategy() {
			methodStrategy(DEFAULT_STRATEGY_METHOD);
			return this;
		}

		public Builder methodStrategy(PlacementStrategy methodStrategy) {
			this.methodStrategy = methodStrategy;
			return this;
		}

		public Builder useDefaultCtorStrategy() {
			ctorStrategy(DEFAULT_STRATEGY_CTOR);
			return this;
		}

		public Builder ctorStrategy(PlacementStrategy ctorStrategy) {
			this.ctorStrategy = ctorStrategy;
			return this;
		}

		public Builder useDefaultClassStrategy() {
			typeStrategy(DEFAULT_STRATEGY_CLASS);
			return this;
		}

		public Builder typeStrategy(PlacementStrategy typeStrategy) {
			this.typeStrategy = typeStrategy;
			return this;
		}
	}
}
