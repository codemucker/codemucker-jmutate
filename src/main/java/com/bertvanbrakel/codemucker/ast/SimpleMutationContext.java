package com.bertvanbrakel.codemucker.ast;

import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.PlacementStrategies;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.codemucker.transform.StringTemplate;

public class SimpleMutationContext implements MutationContext {

	private final PlacementStrategies strategyProvider  = PlacementStrategies.newBuilder().build();
	private final JAstParser parser = JAstParser.newBuilder().build();
	
	public SimpleMutationContext(){
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
    	return new SourceTemplate(parser);
    }
    
	@Override
    public StringTemplate newStringTemplate(){
    	return new StringTemplate();
    }
}
