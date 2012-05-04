package com.bertvanbrakel.codemucker.ast;

import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.PlacementStrategies;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.codemucker.transform.StringTemplate;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class SimpleMutationContext implements MutationContext {

	private final PlacementStrategies strategyProvider  = PlacementStrategies.newBuilder().build();
	private final JAstParser parser = JAstParser.newBuilder().build();
	private final Injector injector;

	public SimpleMutationContext(){
		injector = Guice.createInjector(new MutationModule());
	}
	
	private class MutationModule extends AbstractModule {

		@Override
        protected void configure() {
        }
		
		@Provides
		@Singleton
		public MutationContext provideContext(){
			return SimpleMutationContext.this;
		}
		
		@Provides
		@Singleton
		public PlacementStrategies provideStrategies(){
			return strategyProvider;
		}
		
		@Provides
		@Singleton
		public JAstParser provideParser(){
			return parser;
		}	
	}

	@Override
    public PlacementStrategies getStrategies() {
	    return strategyProvider;
    }

	@Override
    public JAstParser getParser() {
	    return parser;
    }
	
	@Override
    public SourceTemplate newSourceTemplate(){
    	return create(SourceTemplate.class);
    }
    
	@Override
    public StringTemplate newStringTemplate(){
    	return create(StringTemplate.class);
    }
	
	@Override
	public <T> T create(Class<T> type){
		return injector.getInstance(type);
	}
	
	@Override
	public <T> T fill(T instance){
		injector.injectMembers(instance);
		return instance;
	}
	
	
}
