package org.codemucker.jmutate.generate;

import org.codemucker.jmutate.ClashStrategyResolver;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.eclipse.jdt.core.dom.ASTNode;


/**
 * A clash resolver which only replaces nodes owned by the given generator, else uses
 * the fallback one
 */
public class ReplaceOnlyOwnedNodesResolver implements ClashStrategyResolver{

	private final ClashStrategy fallbackStrategy;
	private final CodeGenMetaGenerator meta;
	
	public ReplaceOnlyOwnedNodesResolver(CodeGenMetaGenerator meta, ClashStrategy fallbackStrategy) {
		super();
		this.fallbackStrategy = fallbackStrategy;
		this.meta = meta;
	}

	@Override
	public ClashStrategy resolveClash(ASTNode existingNode,ASTNode newNode) {
		if(meta.isManagedByThis(JMethod.from(existingNode).getAnnotations())){
			return ClashStrategy.REPLACE;
		}
		return fallbackStrategy;
	}
	
}
