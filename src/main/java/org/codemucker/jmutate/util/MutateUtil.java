package org.codemucker.jmutate.util;

import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.ResourceLoader;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Provides some convenience methods that don't seem to fit anywhere else
 * 
 */
public class MutateUtil  {

    private static final String NODE_PROPERTY_RESOURCE_LOADER = MutateUtil.class.getSimpleName() + ":Loader";
    private static final String NODE_PROPERTY_RESOURCE = MutateUtil.class.getSimpleName() + ":resource";

    private static ClassLoader classLoader;
    
    public static ResourceLoader getResourceLoader(ASTNode node){
        return (ResourceLoader) node.getRoot().getProperty(NODE_PROPERTY_RESOURCE_LOADER);
    }
    
    public static void setResourceLoader(ASTNode node, ResourceLoader loader){
        node.getRoot().setProperty(NODE_PROPERTY_RESOURCE_LOADER, loader);    
    }
    
    public static RootResource getResource(ASTNode node){
        return (RootResource) node.getRoot().getProperty(NODE_PROPERTY_RESOURCE);
    }
    
    public static void setResource(ASTNode node, RootResource resource){
        node.getRoot().setProperty(NODE_PROPERTY_RESOURCE, resource);    
    }
    
	public static Class<?> loadClassOrNull(String className){
		ClassLoader loader = getClassLoaderForResolving();
		try {
			return loader.loadClass(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	public static void setClassLoader(ClassLoader classloader){
	    MutateUtil.classLoader = classloader;
	}
	
	//ideally like to inject this in somehow. Worst case we do this via a static (yuck)
    public static ClassLoader getClassLoaderForResolving() {
        ClassLoader cl = classLoader;
        if (cl == null) {
            // TODO:possibly want one which doesn't cache the classes?
            cl = Thread.currentThread().getContextClassLoader();
        }
        return cl;
    }

	public static boolean canLoadClass(ClassLoader cl, String className){
		try {
            cl.loadClass(className);
			return true;
		} catch (ClassNotFoundException e) {
			// do nothing. Just try next prefix
		} catch (NoClassDefFoundError e) {
			// do nothing. Just try next prefix
		}
		return false;
	}
	
	
}
