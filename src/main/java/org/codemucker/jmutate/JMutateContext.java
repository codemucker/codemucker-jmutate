package org.codemucker.jmutate;

import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.ToSourceConverter;
import org.codemucker.jmutate.generate.JAnnotationCompiler;
import org.codemucker.jtest.ProjectLayout;
import org.eclipse.jdt.core.dom.ASTNode;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultMutateContext.class)
public interface JMutateContext {

	/**
	 * Shortcut for {@link #obtain(SourceTemplate.class)}. Returns a source template using the default generation root
	 * 
	 * @return
	 */
	SourceTemplate newSourceTemplate();


	/**
	 * Returns a new source template using a temporary generation root unique to this template
	 * @return
	 */
    SourceTemplate newTempSourceTemplate();
    
	/**
	 * Obtain an instance of the given class. This may be a singleton or a new instance and may or may
	 * not be fully initialised depending on the class. This allows callers to be decoupled from
	 * much of the initialisation and hence the need to pass loads of parameters around
	 * 
	 * @param type
	 * @return
	 */
	<T> T obtain(Class<T> type);
	
	/**
	 * Get hold of the parser used. Shortcut for {@link #obtain(JAstParser.class)}
	 * 
	 * @return
	 */
	JAstParser getParser();
	
	/**
	 * Get hold of the compiler used. Shortcut for {@link #obtain(JCompiler.class)}
	 * @return
	 */
	JCompiler getCompiler();
	
	JAnnotationCompiler getAnnotationCompiler();
    
	ToSourceConverter getNodeToSourceConverter();
	
	/**
	 * Where code is generated to by default
	 * @return
	 */
	Root getDefaultGenerationRoot();
	
	ProjectLayout getProjectLayout();
	
	/**
	 * Return the resource loader used for resolving resources and classnames
	 * @return
	 */
	ResourceLoader getResourceLoader();

	void trackChanges(ASTNode node);

	/**
	 * Register the given source as being modified or created
	 * 
	 * @param source
	 */
	void trackChanges(JSourceFile source);


	/**
	 * Load the given the resource as a source file. If there is a source which has been modified with the same resource path, return this
	 * @param resource
	 * @return
	 */
	JSourceFile getOrLoadSource(RootResource resource);

		
}
