package org.codemucker.jmutate;

import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.eclipse.jdt.core.dom.ASTNode;

public interface SourceLoader {

	void trackChanges(ASTNode node);

	void trackChanges(IProvideCompilationUnit provider);

    boolean canLoadClassOrSource(String fullClassName);

    JType loadTypeForClass(Class<?> klass);

	/**
	 * Load the type for the given classname or null if it couldn't be found
	 * 
	 * @param fullName
	 * @return
	 */
	JType loadTypeForClass(String fullName);

	JSourceFile loadSourceForClass(Class<?> klass);

	JSourceFile loadSourceForClass(String fullName);

	JSourceFile loadSourceFrom(RootResource resource);
    
    public Class<?> loadClass(String fullClassName) throws ClassNotFoundException;
    
    Class<?> loadClassOrNull(String fullClassName);
    
}