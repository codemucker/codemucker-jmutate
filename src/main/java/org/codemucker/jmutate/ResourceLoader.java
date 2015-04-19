package org.codemucker.jmutate;

import java.util.Collection;

import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;

/**
 * Provide an aggregated view over a list of {@link Root}'s. May provide caching. Similar in concept to classloaders except works on 
 * uncompiled source code too. Might defer to the configured classloader when looking for classes
 *  
 */
public interface ResourceLoader {
    
    boolean containsResource(String relPath);

    RootResource getResourceOrNull(String relPath);
    

    /**
     * Return all the roots used in this loader, including those of any parent loaders (if any)
     * @return
     */
    public Collection<Root> getAllRoots();
    
    /**
     * Does a class with the given full name exist either as a source file (so uncompiled) or in the the classpath heiarchy? Results might be cached for faster lookup
     * @param fullClassName
     * @return
     */
    public boolean canLoadClassOrSource(String fullClassName);
    
    public Class<?> loadClass(String fullClassName) throws ClassNotFoundException;
    
    Class<?> loadClassOrNull(String fullClassName);
    
    ClassLoader getClassLoader();
}
