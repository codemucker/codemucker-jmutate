package org.codemucker.jmutate;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;
import org.codemucker.lang.IBuilder;
import org.codemucker.lang.annotation.Optional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class DefaultResourceLoader implements ResourceLoader {
 
    private static final Logger log = LogManager.getLogger(DefaultResourceLoader.class);

    private final ImmutableList<Root> roots;
    private final ImmutableList<Root> rootsAndParent;
    private final ClassLoader classLoader;
    private final ResourceLoader parent;
    
    private final Cache<String, Boolean> classExistsCache;
    
    public static Builder with(){
        return new Builder();
    }
    
    private DefaultResourceLoader(Iterable<Root> roots,ClassLoader classLoader,ResourceLoader parent, int maxCacheEntries, long expireCacheTime,TimeUnit expireCacheUnits) {
        super();
        this.classLoader = classLoader;
        this.roots = ImmutableList.copyOf(roots);
        if (parent != null) {
            this.rootsAndParent = ImmutableList.<Root>builder().addAll(parent.getAllRoots()).addAll(roots).build();
        } else {
            this.rootsAndParent = this.roots;
        }

        this.classExistsCache = CacheBuilder.newBuilder().expireAfterAccess(expireCacheTime, expireCacheUnits).maximumSize(maxCacheEntries).build();
        this.parent = parent;
        List<URL> urls = new ArrayList<>();
        for (Root root : roots) {
            urls.add(root.toURL());
        }
    }

    @Override
    public boolean containsResource(String relPath){
        if(parent!= null && parent.containsResource(relPath)){
            return true;
        }
        for(Root root:roots){
            if(root.canReadResource(relPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public RootResource getResourceOrNull(String relPath){
        if(parent != null){
            RootResource resource = parent.getResourceOrNull(relPath);
            if(resource != null){
                return resource;
            }
        }
        
        for(Root root:roots){
            if(root.canReadResource(relPath)){
                return root.getResource(relPath);
            }
        }
        return null;
    }

    @Override
    public Collection<Root> getAllRoots() {
        return rootsAndParent;
    }

    @Override
    public boolean canLoadClassOrSource(final String fullClassName) {
        if (parent != null && parent.canLoadClassOrSource(fullClassName)) {
            return true;
        }
        try {
            return classExistsCache.get(fullClassName, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return internalLookupCanLoadClassOrSource(fullClassName);
                }
            });
        } catch (ExecutionException e) {
            log.warn("error while determinign if class '" + fullClassName + "' exists", e);
            return false;
        }
    }
    
    @Override
    public Class<?> loadClassOrNull(String fullClassName) {
        try {
        	return loadClass(fullClassName);
        }catch (ClassNotFoundException e){
        	//do nothing
        }
        return null;
    }
    
    @Override
    public Class<?> loadClass(String fullClassName) throws ClassNotFoundException {
        if (parent != null) {
            try {
                Class<?> loadedClass = parent.loadClass(fullClassName);
                return loadedClass;
            } catch(ClassNotFoundException e){
                
            }
        }
       if(classLoader != null){
            return classLoader.loadClass(fullClassName);
       }
       throw new ClassNotFoundException("Could not find class " + fullClassName);
    }

    private boolean internalLookupCanLoadClassOrSource(String fullClassName) {
        String resourcePathClass =  toCompiledClassResourcePath(fullClassName);
        String resourcePathSrc = fullClassName.replace('.', '/') + ".java";
        for (Root root : roots) {
            if (root.canReadResource(resourcePathClass) || root.canReadResource(resourcePathSrc)) {
                return true;
            }
        }
        //last resort!
        if (classLoader != null) {
            try {
                classLoader.loadClass(fullClassName);
            	return true;
            } catch (ClassNotFoundException e) {
            	// do nothing. Just try next prefix
            } catch (NoClassDefFoundError e) {
            	// do nothing. Just try next prefix
            }
        }
        return false;
    }

    /**
     * convert com.mycompany.Foo.Bar to com/mycomany/Foo$Bar.class and com/mycomany/Foo.java!Bar
     * 
     */
    private static String toCompiledClassResourcePath(String fullClassName) {
        StringBuilder sb = new StringBuilder();
        boolean inClass = false;
        for (int i = 0; i < fullClassName.length(); i++) {
            char c = fullClassName.charAt(i);
            if (c == '.') {
                if (inClass) {
                    sb.append('$');
                } else {
                    sb.append('/');
                }
            } else if (Character.isUpperCase(c)) {
                inClass = true;
                sb.append(c);
            } else {
                sb.append(c);
            }
        }
        sb.append(".class");
        return sb.toString();
    }

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("@").append(hashCode()).append("[");
		
		sb.append("roots=[");
		boolean comma = false;
		for(Root root:roots){
			if(comma){
				sb.append(",\n\t");
			}
			sb.append(root);
			comma = true;
		}
		sb.append("], parent=");
		sb.append(parent);
		sb.append("]");
		return sb.toString();
	}

    
    public static class Builder implements IBuilder<DefaultResourceLoader> {
        private ClassLoader classLoader;
        private ResourceLoader parent;
        private List<Root> additionalRoots = new ArrayList<>();
        private long expireCacheTime = 1;
        private TimeUnit expireCacheUnits = TimeUnit.MINUTES;
        private int maxCacheEntries = 5000;
        
        @Override
        public DefaultResourceLoader build() {
            ClassLoader cl = getClassLoaderOrDefault(parent);
            return new DefaultResourceLoader(additionalRoots, cl, parent, maxCacheEntries, expireCacheTime, expireCacheUnits);
        }
        
        private ClassLoader getClassLoaderOrDefault(ResourceLoader parent){
        	if(this.classLoader !=null){
        		return this.classLoader;
        	}
            ClassLoader newClassLoader = Thread.currentThread().getContextClassLoader();
            
			if (parent != null) {
				ClassLoader parentClassLoader = parent.getClassLoader();
				if (parentClassLoader != null && parentClassLoader != newClassLoader) {
					newClassLoader = new URLClassLoader(new URL[] {},parentClassLoader);
				}
			}
            Set<URL> urls = new LinkedHashSet<>();
            for (Root r : additionalRoots) {
                urls.add(r.toURL());
            }
            newClassLoader = new URLClassLoader(urls.toArray(new URL[]{}), newClassLoader);
            return newClassLoader; 
        }
        
        @Optional
        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        @Optional
        public Builder parentLoader(ResourceLoader parent) {
            this.parent = parent;
            return this;
        }

        @Optional
        public Builder roots(IBuilder<? extends Iterable<Root>> builder) {
            this.roots(builder.build());
            return this;
        }
        
        @Optional
        public Builder roots(Iterable<Root> additionalRoots) {
            this.additionalRoots = Lists.newArrayList(additionalRoots);
            return this;
        }
        
        @Optional
        public Builder addRoot(Root root) {
            this.additionalRoots.add(root);
            return this;
        }

    }

}
