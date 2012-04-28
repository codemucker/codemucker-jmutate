package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.transform.PlacementStrategy;

public class PlacementStrategies {

	private final PlacementStrategy fieldStrategy;
	private final PlacementStrategy methodStrategy;
	private final PlacementStrategy ctorStrategy;
	private final PlacementStrategy typeStrategy;
	
	public static Builder newBuilder(){
		return new Builder();
	}
	
	public PlacementStrategies(
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

	private static Collection<Class<?>> col(Class<?>... types) {
		return Arrays.asList(types);
	}
	
	public static class Builder {

		private static final PlacementStrategy DEFAULT_STRATEGY_FIELD = new StrategyBeforeAfterNodes(
				col(FieldDeclaration.class),
			    col(MethodDeclaration.class, TypeDeclaration.class, EnumDeclaration.class));
		private static final PlacementStrategy DEFAULT_STRATEGY_METHOD = new StrategyBeforeAfterNodes(
				col(MethodDeclaration.class, FieldDeclaration.class, EnumDeclaration.class),
		        col(TypeDeclaration.class));
		private static final PlacementStrategy DEFAULT_STRATEGY_CTOR = new StrategyBeforeAfterNodes(
				col(FieldDeclaration.class, EnumDeclaration.class), 
				col(TypeDeclaration.class));
		private static final PlacementStrategy DEFAULT_STRATEGY_CLASS = new StrategyBeforeAfterNodes(
				col(FieldDeclaration.class, MethodDeclaration.class, EnumDeclaration.class, TypeDeclaration.class),
			    col());

		private PlacementStrategy fieldStrategy;
		private PlacementStrategy methodStrategy;
		private PlacementStrategy ctorStrategy;
		private PlacementStrategy typeStrategy;

		public Builder(){
			setUseDefaultClassStrategy();
			setUseDefaultCtorStrategy();
			setUseDefaultFieldStrategy();
			setUseDefaultMethodStrategy();
		}
		
		public PlacementStrategies build(){
			return new PlacementStrategies(fieldStrategy,ctorStrategy,methodStrategy,typeStrategy);
		}
		
		public Builder setUseDefaultFieldStrategy() {
			setFieldStrategy(DEFAULT_STRATEGY_FIELD);
			return this;
		}
		
		public Builder setFieldStrategy(PlacementStrategy fieldStrategy) {
			this.fieldStrategy = fieldStrategy;
			return this;
		}

		public Builder setUseDefaultMethodStrategy() {
			setMethodStrategy(DEFAULT_STRATEGY_METHOD);
			return this;
		}

		public Builder setMethodStrategy(PlacementStrategy methodStrategy) {
			this.methodStrategy = methodStrategy;
			return this;
		}

		public Builder setUseDefaultCtorStrategy() {
			setCtorStrategy(DEFAULT_STRATEGY_CTOR);
			return this;
		}

		public Builder setCtorStrategy(PlacementStrategy ctorStrategy) {
			this.ctorStrategy = ctorStrategy;
			return this;
		}

		public Builder setUseDefaultClassStrategy() {
			setTypeStrategy(DEFAULT_STRATEGY_CLASS);
			return this;
		}

		public Builder setTypeStrategy(PlacementStrategy typeStrategy) {
			this.typeStrategy = typeStrategy;
			return this;
		}
		
	}
}
