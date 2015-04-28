package org.codemucker.jmutate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;
import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.base.Joiner;
import com.google.inject.Inject;

public class DefaultSourceLoader implements SourceLoader {

	private static final Logger LOG = LogManager.getLogger(DefaultSourceLoader.class);
	
	private final Map<String, LoadedSourceTracker> sourceTrackersByRootPath = new HashMap<>();
	private final Map<String, RootResource> resourcesByClassName = new HashMap<>();
	
	private final JAstParser parser;
	private final ResourceLoader resourceLoader;
	
	private final boolean failOnNotFind;
	
	@Inject
	public DefaultSourceLoader(ResourceLoader loader,JAstParser parser) {
		this(loader,parser,false);
	}
	
	public DefaultSourceLoader(ResourceLoader loader,JAstParser parser, boolean failOnNotFind) {
		this.resourceLoader = loader;
		this.parser = parser;
		this.failOnNotFind = failOnNotFind;
	}

	@Override
	public void trackChanges(ASTNode node) {
		JSourceFile source = MutateUtil.getSource(node);
		if(source != null){
			trackChanges(source);
		}
	}

	@Override
	public void trackChanges(IProvideCompilationUnit provider) {
		JSourceFile source = null;
		if (provider != null) {
			source = provider.getCompilationUnit().getSource();
		}
		if (source != null) {
			getOrCreateTrackerFor(source.getResource()).addSource(source);
		} else {
			LOG.debug("no source file for provied supplier");
		}
	}
	
	@Override
	public JType loadTypeForClass(Class<?> klass){
		return loadTypeForClass(klass.getName());
	}
	
	@Override
	public JType loadTypeForClass(String fullName){
		
		JSourceFile source = loadSourceForClass(fullName);
		JType type = null;
		if (source != null) {
			String cleanFullName = NameUtil.compiledNameToSourceName(fullName);
			type = source.findTypesMatching(AJType.with().fullName(cleanFullName)).getFirstOrNull();		
			if(type == null){
				LOG.warn("Can't find type '" + cleanFullName + "' in " + source.getResource().getFullPath() + ", using resource loader " + resourceLoader);
			}
		}
		if(type == null && failOnNotFind){
			throw new NoSuchElementException("could not find typenode for " + fullName + ", using resource loader " + resourceLoader);		
		}
		return type;
	}
	
	@Override
	public JSourceFile loadSourceForClass(Class<?> klass){
		return loadSourceForClass(klass.getName());
	}
	
	@Override
	public JSourceFile loadSourceForClass(String fullName){
		JSourceFile source = null;
		RootResource resource = getResourceForClass(fullName);
		if (resource != null && resource.exists()) {
			source = loadSourceFrom(resource);
		}
		if(source == null && failOnNotFind){
			throw new NoSuchElementException("could not find source for " + fullName + ", using resource loader " + resourceLoader);	
		}
		return source;
	}

	@Override
	public JSourceFile loadSourceFrom(RootResource resource) {
		if(resource == null){
			return null;
		}
		LoadedSourceTracker tracker = getOrCreateTrackerFor(resource);
		JSourceFile source = tracker.getSource(resource);
		if(source == null){
			source = JSourceFile.fromResource(resource, parser);
			tracker.addSource(source);
		} else {
			LOG.debug("already have chaneg tracker for " + source.getResource().getFullPath());
		}
		
		return source;
	}

	private LoadedSourceTracker getOrCreateTrackerFor(RootResource resource){
		String rootPath = resource.getRoot().getFullPath();
		LoadedSourceTracker tracker = sourceTrackersByRootPath.get(rootPath);			
		if(tracker == null){
			LOG.debug("created tracker for " + resource.getFullPath());
			tracker = new LoadedSourceTracker(rootPath);
			sourceTrackersByRootPath.put(rootPath,tracker);
		}
		return tracker;
	}

    private RootResource getResourceForClass(String fullClassName) {
		RootResource r = resourcesByClassName.get(fullClassName);
		if (r != null) {
			return r;
		}
    	//if of type com.mycompany.Foo$Bar we can look direct for Fcom.mycompany.Foo
    	String relPath;
    	int dollar  = fullClassName.indexOf('$');
    	if(dollar !=-1){
    		relPath = fullClassName.substring(0, dollar).replace(".", "/") + ".java";
    	} else {
    		relPath = fullClassName.replace(".", "/") + ".java";
    	}
    	List<String> tried = new ArrayList<>();
    	r = resourceLoader.getResourceOrNull(relPath);
    	tried.add(relPath);
    	if(r == null){
    		//is it an inner class path? so com.mycompany.Foo.Bar or com.mycompany.Foo$Bar
    		for( int i = fullClassName.length() - 1; i >=0 && r == null;i--){
    			if(fullClassName.charAt(i) == '.'){
    				relPath = fullClassName.substring(0, i).replace(".", "/")  + ".java";
    				tried.add(relPath);
    				r = resourceLoader.getResourceOrNull(relPath);
    			}
    		}
    	}
		if (r != null) {
			resourcesByClassName.put(fullClassName, r);
		}
		if(r == null && failOnNotFind){
			throw new NoSuchElementException("could not find source for " + fullClassName + ", tried [" + Joiner.on(',').join(tried) + "], using resource loader " + resourceLoader);	
		}
    	return r;
    };
    
    @Override
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
    
    @Override
    public String toString() {
    	return getClass().getName() + "@" + hashCode() + "[resourceLoader:" + resourceLoader + "]";
    }

	private static class LoadedSourceTracker {
    	private final String rootPath;
    	private final Map<String, JSourceFile> sourcesByResourcePath = new HashMap<>();
    	
		public LoadedSourceTracker(String rootPath) {
			super();
			this.rootPath = rootPath;
		}
		
		public JSourceFile getSource(RootResource resource){
			String key = resource.getRelPath();
			JSourceFile source = sourcesByResourcePath.get(key);
			return source;
		}
		
		public void addSource(JSourceFile source){
			String key = source.getResource().getRelPath();
			sourcesByResourcePath.put(key, source);
		}
    }

}