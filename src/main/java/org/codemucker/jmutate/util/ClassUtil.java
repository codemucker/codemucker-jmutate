package org.codemucker.jmutate.util;

/**
 * Provides some convenience methods
 * 
 */
public class ClassUtil {

	public static Class<?> loadClassOrNull(String className){
		ClassLoader loader = getClassLoaderForResolving();
		try {
			return loader.loadClass(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	public static boolean canLoadClass(String className){
		return canLoadClass(getClassLoaderForResolving(), className);
	}
	
	//ideally like to inject this in somehow. Worst case we do this via a static (yuck)
	public static ClassLoader getClassLoaderForResolving(){
		//TODO:possibly want one which doesn't cache the classes?
		return Thread.currentThread().getContextClassLoader();
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
