package com.bertvanbrakel.codemucker.ast;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.transform.PlacementStrategy;

public class PlacementStrategies {

	public static final PlacementStrategy STRATEGY_FIELD = new StrategyBeforeAfterNodes(
			col(FieldDeclaration.class),
		    col(MethodDeclaration.class, TypeDeclaration.class, EnumDeclaration.class));
	public static final PlacementStrategy STRATEGY_METHOD = new StrategyBeforeAfterNodes(
			col(MethodDeclaration.class, FieldDeclaration.class, EnumDeclaration.class),
	        col(TypeDeclaration.class));
	public static final PlacementStrategy STRATEGY_CTOR = new StrategyBeforeAfterNodes(
			col(FieldDeclaration.class, EnumDeclaration.class), 
			col(TypeDeclaration.class));
	public static final PlacementStrategy STRATEGY_CLASS = new StrategyBeforeAfterNodes(
			col(FieldDeclaration.class, MethodDeclaration.class, EnumDeclaration.class, TypeDeclaration.class),
		    col());

	public PlacementStrategy getFieldStrategy(){
		return STRATEGY_FIELD;
	}
	
	public PlacementStrategy getMethodStrategy(){
		return STRATEGY_METHOD;
	}
	
	public PlacementStrategy getCtorStrategy(){
		return STRATEGY_CTOR;
	}
	
	public PlacementStrategy getClassStrategy(){
		return STRATEGY_CLASS;
	}

	private static Collection<Class<?>> col(Class<?>... types) {
		return Arrays.asList(types);
	}
}
