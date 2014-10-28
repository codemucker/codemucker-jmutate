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

    RootResource getResource(String relPath);

    /**
     * Return all the roots used in this loader, including those of any parent loaders (if any)
     * @return
     */
    public Collection<Root> getAllRoots();
    
    /**
     * DOes a class witht eh given full name exist either as a source file (so uncompiled) or in the the classpath heiarchy? Results might be cached for faster lookup
     * @param fullClassName
     * @return
     */
    public boolean canLoadClassOrSource(String fullClassName);
    
}
