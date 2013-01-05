package com.bertvanbrakel.codemucker.ast.finder;

import org.eclipse.jdt.core.dom.ASTParser;

import com.bertvanbrakel.codemucker.ast.JAstParser;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Stage;

public class FinderContext {

	private final Injector injector;
	
	public static Builder builder(){
		return new Builder();
	}
	
	public FinderContext(){
		injector = Guice.createInjector(Stage.PRODUCTION, new FinderContextModule());
	}
	
	
	public <T> T obtain(Class<T> type){
		return injector.getInstance(type);
	}
	
	
	private class FinderContextModule extends AbstractModule {

		@Override
		protected void configure() {
		}
		
		@Provides
		public ASTParser provideAstParser(){
			return provideJAstParser().getASTParser();
		}
		
		@Provides
		public JAstParser provideJAstParser(){
			return JAstParser.builder().build();
		}
	}
	
	public static class Builder {
		
		public FinderContext build(){
			return new FinderContext();
		}
	}
}
