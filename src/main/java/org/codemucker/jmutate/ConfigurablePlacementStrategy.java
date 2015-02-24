package org.codemucker.jmutate;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.StrategyBeforeAfterNodes;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class ConfigurablePlacementStrategy  implements PlacementStrategy {

	private final PlacementStrategy fieldStrategy;
	private final PlacementStrategy methodStrategy;
	private final PlacementStrategy ctorStrategy;
	private final PlacementStrategy typeStrategy;
	private final PlacementStrategy defaultStrategy;
	
	public static Builder with(){
		return new Builder();
	}
	
	private ConfigurablePlacementStrategy(
			PlacementStrategy fieldStrategy
			, PlacementStrategy ctorStrategy
            , PlacementStrategy methodStrategy
            , PlacementStrategy typeStrategy
            , PlacementStrategy defaultStrategy
			) {
	    super();
	    this.fieldStrategy = checkNotNull(fieldStrategy,"expect field strategy");
	    this.methodStrategy = checkNotNull(methodStrategy,"expect method strategy");
	    this.ctorStrategy = checkNotNull(ctorStrategy,"expect ctor strategy");
	    this.typeStrategy = checkNotNull(typeStrategy,"expect class strategy");
	    this.defaultStrategy = checkNotNull(typeStrategy,"expect default strategy");
	}


	@Override
	public int findIndexToPlaceInto(ASTNode nodeToInsert, List<ASTNode> nodes) {
		return findStrategy(nodeToInsert).findIndexToPlaceInto(nodeToInsert, nodes);
	}
	
	private PlacementStrategy findStrategy(ASTNode nodeToInsert) {
		if (JField.is(nodeToInsert)) {
			return fieldStrategy;
		} else if (JMethod.is(nodeToInsert)) {
			MethodDeclaration md = (MethodDeclaration) nodeToInsert;
			if (md.isConstructor()) {
				return ctorStrategy;
			} else {
				return methodStrategy;
			}
		} else if (JType.is(nodeToInsert)) {
			return typeStrategy;
		}
		return defaultStrategy;
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
			.beforeNodes(TypeDeclaration.class,MethodDeclaration.class)
			.build();
		private static final PlacementStrategy DEFAULT_STRATEGY_CLASS = StrategyBeforeAfterNodes.with()
			.afterNodes(FieldDeclaration.class, MethodDeclaration.class, EnumDeclaration.class, TypeDeclaration.class)
			.build();

		private PlacementStrategy fieldStrategy;
		private PlacementStrategy methodStrategy;
		private PlacementStrategy ctorStrategy;
		private PlacementStrategy typeStrategy;
		private PlacementStrategy defaultStrategy;

		private Builder(){
		}		

		public ConfigurablePlacementStrategy build(){
			return new ConfigurablePlacementStrategy(fieldStrategy,ctorStrategy,methodStrategy,typeStrategy, defaultStrategy==null?DEFAULT_STRATEGY_CLASS:defaultStrategy);
		}

		public Builder defaults(){
			defaultClassStrategy();
			defaultCtorStrategy();
			defaultFieldStrategy();
			defaultMethodStrategy();	

			return this;
		}

		public Builder defaultStrategy(PlacementStrategy strategy) {
			this.defaultStrategy = strategy;
			return this;
		}

		public Builder defaultFieldStrategy() {
			fieldStrategy(DEFAULT_STRATEGY_FIELD);
			return this;
		}
		
		public Builder fieldStrategy(PlacementStrategy strategy) {
			this.fieldStrategy = strategy;
			return this;
		}

		public Builder defaultMethodStrategy() {
			methodStrategy(DEFAULT_STRATEGY_METHOD);
			return this;
		}

		public Builder methodStrategy(PlacementStrategy strategy) {
			this.methodStrategy = strategy;
			return this;
		}

		public Builder defaultCtorStrategy() {
			ctorStrategy(DEFAULT_STRATEGY_CTOR);
			return this;
		}

		public Builder ctorStrategy(PlacementStrategy strategy) {
			this.ctorStrategy = strategy;
			return this;
		}

		public Builder defaultClassStrategy() {
			typeStrategy(DEFAULT_STRATEGY_CLASS);
			return this;
		}

		public Builder typeStrategy(PlacementStrategy strategy) {
			this.typeStrategy = strategy;
			return this;
		}
	}

}
