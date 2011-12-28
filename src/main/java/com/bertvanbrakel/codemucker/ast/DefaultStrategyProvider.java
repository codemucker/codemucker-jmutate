package com.bertvanbrakel.codemucker.ast;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class DefaultStrategyProvider {

	private static final InsertionStrategy STRATEGY_FIELD = new StrategyBeforeAfterNodes(
			col(FieldDeclaration.class),
		    col(MethodDeclaration.class, TypeDeclaration.class, EnumDeclaration.class));
	private static final InsertionStrategy STRATEGY_METHOD = new StrategyBeforeAfterNodes(
			col(MethodDeclaration.class, FieldDeclaration.class, EnumDeclaration.class),
	        col(TypeDeclaration.class));
	private static final InsertionStrategy STRATEGY_CTOR = new StrategyBeforeAfterNodes(
			col(FieldDeclaration.class, EnumDeclaration.class), 
			col(TypeDeclaration.class));
	private static final InsertionStrategy STRATEGY_CLASS = new StrategyBeforeAfterNodes(
			col(FieldDeclaration.class, MethodDeclaration.class, EnumDeclaration.class, TypeDeclaration.class),
		    col());

	public InsertionStrategy getFieldStrategy(){
		return STRATEGY_FIELD;
	}
	
	public InsertionStrategy getMethodStrategy(){
		return STRATEGY_METHOD;
	}
	
	public InsertionStrategy getCtorStrategy(){
		return STRATEGY_CTOR;
	}
	
	public InsertionStrategy getClassStrategy(){
		return STRATEGY_CLASS;
	}

	private static Collection<Class<?>> col(Class<?>... types) {
		return Arrays.asList(types);
	}
}
