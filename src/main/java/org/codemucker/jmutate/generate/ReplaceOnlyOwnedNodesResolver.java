package org.codemucker.jmutate.generate;

import org.codemucker.jmutate.ClashStrategyResolver;
import org.codemucker.jmutate.ast.Annotations;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.eclipse.jdt.core.dom.ASTNode;


/**
 * A clash resolver which only replaces nodes owned by the given generator, else uses
 * the fallback one
 */
public class ReplaceOnlyOwnedNodesResolver implements ClashStrategyResolver{

	private final ClashStrategy fallbackStrategy;
	private final ClashStrategy managedStrategy = ClashStrategy.REPLACE;
	private final CodeGenMetaGenerator meta;
	
	public ReplaceOnlyOwnedNodesResolver(CodeGenMetaGenerator meta, ClashStrategy fallbackStrategy) {
		super();
		this.fallbackStrategy = fallbackStrategy;
		this.meta = meta;
	}

	@Override
	public ClashStrategy resolveClash(ASTNode existingNode,ASTNode newNode) {
		Annotations annotations;
		if(JMethod.is(existingNode)){
			annotations = JMethod.from(existingNode).getAnnotations();
		} else if(JField.is(existingNode)){
			annotations = JField.from(existingNode).getAnnotations();
		} else if(JType.is(existingNode)){
			annotations = JType.from(existingNode).getAnnotations();
		} else {
			return fallbackStrategy;
		}
		if(meta.isManagedByThis(annotations)){
			return managedStrategy;
		}
		return fallbackStrategy;
	}
	
}
