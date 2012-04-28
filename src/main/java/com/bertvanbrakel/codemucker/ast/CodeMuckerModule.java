package com.bertvanbrakel.codemucker.ast;

import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class CodeMuckerModule  extends AbstractModule {

	private MutationContext ctxt = new SimpleMutationContext();
	
	@Override
    protected void configure() {
    }
	
	@Provides
	public MutationContext provideMutationContext(){
		return ctxt;
	}
	
	@Provides
	@Singleton
	public PlacementStrategies providePlacementStrategies(){
		return ctxt.getStrategies();
	}
	
	@Provides
	@Singleton
	public JAstParser provideJAstParser(){
		return ctxt.getParser();
	}
	
	@Provides
	public SourceTemplate provideSourceTemplate(){
		return ctxt.newSourceTemplate();
	}
}
