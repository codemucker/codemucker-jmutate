package org.codemucker.jmutate.util;

import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.ResourceLoader;
import org.codemucker.jmutate.SourceLoader;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Provides some convenience methods that don't seem to fit anywhere else
 * 
 */
public class MutateUtil  {

	private static final String NODE_PROPERTY_RESOURCE_LOADER = MutateUtil.class.getSimpleName() + ":rLoader";
	private static final String NODE_PROPERTY_SOURCE_LOADER = MutateUtil.class.getSimpleName() + ":sLoader";
	private static final String NODE_PROPERTY_SOURCE_FILE = MutateUtil.class.getSimpleName() + ":sFile";

    private static ClassLoader classLoader;
    
    public static ResourceLoader getResourceLoader(ASTNode node){
        return (ResourceLoader) node.getRoot().getProperty(NODE_PROPERTY_RESOURCE_LOADER);
    }
    
    public static SourceLoader getSourceLoaderOrFail(ASTNode node){
       SourceLoader loader = (SourceLoader) node.getRoot().getProperty(NODE_PROPERTY_SOURCE_LOADER);
       if(loader == null){
    	   throw new IllegalStateException("no source loader attached to node");
       }
       return loader;
    }
    
    public static JSourceFile getSource(ASTNode node){
        return (JSourceFile) node.getRoot().getProperty(NODE_PROPERTY_SOURCE_FILE);
    }
    
    public static void setSource(ASTNode node, JSourceFile source){
        node.getRoot().setProperty(NODE_PROPERTY_SOURCE_FILE, source);    
    }
    
    public static void setResourceLoader(ASTNode node, ResourceLoader resourceLoader){
    	node.getRoot().setProperty(NODE_PROPERTY_RESOURCE_LOADER, resourceLoader);
    }
    
    public static void setSourceLoader(ASTNode node, SourceLoader sourceLoader){
    	node.getRoot().setProperty(NODE_PROPERTY_SOURCE_LOADER, sourceLoader);
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

	//TODO: bit of magic here. Work around ircular dependencies.
	public static NodeParser wrapParser(ResourceLoader resourceLoader,JAstParser parser){
		return new NodeParser(resourceLoader,parser);
	}
	
	/**
	 * Attaches various things to the root node
	 */
	public static class NodeParser implements JAstParser{
		private final JAstParser parser;
		private final ResourceLoader resourceLoader;
		SourceLoader sourceLoader;

		NodeParser(ResourceLoader resourceLoader ,JAstParser parser){
			this.parser = parser;
			this.resourceLoader = resourceLoader;
		}
		
		public void setSourceLoader(SourceLoader sourceLoader){
			if(this.sourceLoader != null){
				throw new IllegalStateException("source loader already been set");
			}
			this.sourceLoader = sourceLoader;
		}
		
		@Override
		public CompilationUnit parseCompilationUnit(CharSequence src,RootResource resource) {
			checkSet();
			CompilationUnit cu = parser.parseCompilationUnit(src, resource);
			MutateUtil.setResourceLoader(cu, resourceLoader);
			MutateUtil.setSourceLoader(cu, sourceLoader);
			return cu;
		}

		@Override
		public ASTNode parseNode(CharSequence src, int kind,RootResource resource) {
			checkSet();
			ASTNode node = parser.parseNode(src, kind, resource);
			
			ASTNode root = node.getParent();
			MutateUtil.setResourceLoader(root, resourceLoader);
			MutateUtil.setSourceLoader(root, sourceLoader);

			return node;
		}
		
		private void checkSet(){
			if(sourceLoader == null){
				throw new IllegalStateException("Expect source loader to be set");
			}
		}
		
	}
	
}
