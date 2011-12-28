package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import com.bertvanbrakel.codemucker.util.SourceUtil;

public class DefaultJContext implements JContext {

	private final AstCreator astCreator;
	
	public DefaultJContext(){
		this(SourceUtil.newDefaultAstCreator());
	}
	
	public DefaultJContext(AstCreator astCreator){
		checkNotNull(astCreator,"extpect an ast creator");
		this.astCreator = astCreator;
	}
	
	@Override
    public AstCreator getAstCreator() {
	    return astCreator;
    }

}
