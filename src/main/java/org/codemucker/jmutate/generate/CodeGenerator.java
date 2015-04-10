package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;

import org.apache.commons.configuration.Configuration;
import org.eclipse.jdt.core.dom.ASTNode;

public interface CodeGenerator<TGenerateOptions extends Annotation> {

	public void beforeRun();
    
	public Configuration getDefaultConfig();
    
	/**
     * Perform the generation on the given node, using the extracted generation options
     * 
     * @param node the node this annotation was found on
     * @param options the annotation with all the generation options. This is extracted from the source code and compiled
     */
    public void generate(ASTNode node,Configuration config);

    
    public void afterRun();
}