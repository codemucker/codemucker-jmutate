package org.codemucker.jmutate;

import org.codemucker.jfind.Root;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.ToSourceConverter;
import org.codemucker.jmutate.generate.JAnnotationCompiler;

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
	
	/**
	 * Return the resource loader used for resolving resources and classnames
	 * @return
	 */
	ResourceLoader getResourceLoader();

}
