package org.codemucker.jmutate;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.BaseRootVisitor;
import org.codemucker.jfind.DefaultFindResult;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.MatchListener;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;
import org.codemucker.jfind.RootVisitor;
import org.codemucker.jfind.Roots;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.NullMatchContext;
import org.codemucker.jmutate.ast.BaseSourceVisitor;
import org.codemucker.jmutate.ast.DefaultJAstParser;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.lang.IBuilder;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Utility class to find source files, java types, and methods.
 * 
 * Usage:
 * 
 * 
 */
public class SourceScanner {

    private static final Logger log = LogManager.getLogger(SourceScanner.class);
    
	private static final String  JAVA_EXTENSION = "java";
	private static final MatchListener<Object> DEFAULT_LISTENER = new MatchListener<Object>() {
		@Override
		public void onMatched(Object obj) {
		}
		
		@Override
		public void onIgnored(Object obj) {
		}

		@Override
		public void onError(Object record, Throwable t) throws Throwable {
		    log.warn(String.format("Error processing '%s'", record),t);
		}
	};
	
	private final Collection<Root> scanRoots;
	private final Matcher<Root> rootMatcher;
	private final Matcher<RootResource> resourceMatcher;
	private final Matcher<JSourceFile> sourceMatcher;
	private final Matcher<JType> typeMatcher;
	private final Matcher<JMethod> methodMatcher;
	
    private final MatchListener<Object> listener;
    private final MatchDiagnostics diagnostics = NullMatchContext.INSTANCE;

	private final boolean failOnParseError;
	
	@Inject
	private final JAstParser parser;

	public static interface SourceMatcher {
		public Matcher<Object> getObjectMatcher();
		public Matcher<Root> getRootMatcher();
		public Matcher<RootResource> getResourceMatcher();
		public Matcher<JSourceFile> getSourceMatcher();
		public Matcher<JType> getTypeMatcher();
		public Matcher<JMethod> getMethodMatcher();
	}
	
	public static Builder with(){
		return new Builder();
	}

	public SourceScanner(
			JAstParser parser
			, Iterable<Root> classPathRoots
			, SourceMatcher matchers
			, MatchListener<Object> listener
			, boolean failOnParseError
			) {
		this(parser,
			classPathRoots,
			matchers.getObjectMatcher(),
			matchers.getRootMatcher(),
			matchers.getResourceMatcher(),			
			matchers.getSourceMatcher(),
			matchers.getTypeMatcher(),
			matchers.getMethodMatcher(),
			listener,
			failOnParseError
		);
	}

	public SourceScanner(
			JAstParser parser
			, Iterable<Root> roots
			, Matcher<Object> objectFilter
			, Matcher<Root> rootFilter
			, Matcher<RootResource> resourceFilter
			, Matcher<JSourceFile> sourceFilter
			, Matcher<JType> typeFilter
			, Matcher<JMethod> methodFilter
			, MatchListener<Object> listener
			, boolean failOnParseError
			) {
		
		checkNotNull(roots, "expect class path roots");
		
		this.parser = checkNotNull(parser, "expect parser");
		this.scanRoots = ImmutableList.<Root>builder().addAll(roots).build();

		this.rootMatcher = join(checkNotNull(rootFilter, "expect root filter"), objectFilter);
		this.resourceMatcher = join(checkNotNull(resourceFilter, "expect resource filter"), objectFilter);
		this.sourceMatcher = join(checkNotNull(sourceFilter, "expect source filter"), objectFilter);
		this.typeMatcher = join(checkNotNull(typeFilter, "expect type filter"), objectFilter);
		this.methodMatcher = join(checkNotNull(methodFilter, "expect method filter"), objectFilter);
	
		this.listener = checkNotNull(listener, "expect find listener");
		this.failOnParseError = failOnParseError;
	}
	
	private static <T> Matcher<T> join(final Matcher<T> matcher,final Matcher<Object> objMatcher){
		return new AbstractNotNullMatcher<T>(){
			@Override
			public boolean matchesSafely(T found, MatchDiagnostics diag) {
				return objMatcher.matches(found) && matcher.matches(found);
			}
		};
	}
	
	public void visit(BaseSourceVisitor visitor) {
		for (JSourceFile srcFile : findSources()) {
			srcFile.accept(visitor);
		}
	}
	
	public FindResult<JMethod> findMethods() {
		return findTypes().transformToMany(typeToMethods()).filter(methodMatcher, listener, diagnostics);
	}
	
	private Function<JType, Iterator<JMethod>> typeToMethods(){
		return new Function<JType,Iterator<JMethod>>(){
			@Override
            public Iterator<JMethod> apply(JType type){
				return type.findMethods().iterator();
			}
		};
	}

	public FindResult<JType> findTypes() {
		return findSources().transformToMany(sourceToTypes()).filter(typeMatcher, listener, diagnostics);
    }
		
	private Function<JSourceFile, Iterator<JType>> sourceToTypes(){
		return new Function<JSourceFile,Iterator<JType>>(){
			@Override
            public Iterator<JType> apply(JSourceFile source){
				return source.findAllTypes().iterator();
			}
		};
	}
	
	public FindResult<JSourceFile> findSources() {
		return findResources().transform(resourceToSource()).filter(sourceMatcher, listener, diagnostics);
	}
	
	private Function<RootResource, JSourceFile> resourceToSource(){
		return new Function<RootResource,JSourceFile>(){
			@Override
            public JSourceFile apply(RootResource resource){
				if(resource.hasExtension(JAVA_EXTENSION)){
				   try {
				        return JSourceFile.fromResource(resource, parser);
				    } catch(JMutateParseException e){
				        if(failOnParseError){
				            throw e;
				        } else {
				            onError(resource, e);
				        }	            
				    }
				}
				//indicate skip this item
				return null;
			}
		};
	}

    private void onError(RootResource resource, JMutateParseException e) {
        try {
            listener.onError(resource, e);
        } catch (RuntimeException rethrown) {
            throw rethrown;
        } catch (Throwable rethrown) {
            throw new JMutateException("Error parsing source", rethrown);
        }
    }

	public FindResult<RootResource> findResources(){
		final List<RootResource> resources = Lists.newArrayListWithExpectedSize(200);	
		RootVisitor visitor = new BaseRootVisitor(){
			@Override
			public boolean visit(Root root) {
				boolean visit = rootMatcher.matches(root, diagnostics);
				if(visit){
					listener.onMatched(root);
				} else {
					listener.onIgnored(root);	
				}
				return visit;
			}
			@Override
			public boolean visit(RootResource resource) {
				boolean visit = resourceMatcher.matches(resource, diagnostics);
				if(visit){
					resources.add(resource);
					listener.onMatched(resource);
				} else {
					listener.onIgnored(resource);	
				}
				return visit;
			}
		};

		for(Root root:scanRoots){
			root.accept(visitor);
		}

		return DefaultFindResult.from(resources);
	}
	
	public FindResult<Root> findRoots() {
		return DefaultFindResult.from(scanRoots).filter(rootMatcher, listener, diagnostics);
	}
	
	public static class Builder {
		private JAstParser parser;
		//used for resolving
		private Roots.Builder roots;
		//used for scanning
        private Roots.Builder scanRoots;
		
		private Matcher<Object> objectMatcher;
		private Matcher<Root> rootMatcher;
		private Matcher<RootResource> resourceMatcher;
		private Matcher<JSourceFile> sourceMatcher;
		private Matcher<JType> typeMatcher;
		private Matcher<JMethod> methodMatcher;
		
		private MatchListener<Object> listener;
        private boolean failOnParseError = true;
		
		public SourceScanner build(){
		    Roots.Builder roots = getRootsOrDefault();
		    Roots.Builder scanRoots = getScanRootsOrDefault();
            JAstParser parser = getParserOrDefault(roots,scanRoots);
            
			return new SourceScanner(
			    parser
				, scanRoots.build()
				, anyIfNull(objectMatcher)
				, anyIfNull(rootMatcher)
				, anyIfNull(resourceMatcher)
				, anyIfNull(sourceMatcher)
				, anyIfNull(typeMatcher)
				, anyIfNull(methodMatcher)
				, listener==null?DEFAULT_LISTENER:listener,
				failOnParseError
			);
		}

		private Roots.Builder getRootsOrDefault(){
		    if(this.roots == null){
		        return Roots.with().all().classpath(true);
		    }
		    return roots;
		}
		
		private Roots.Builder getScanRootsOrDefault(){
            if(this.scanRoots == null){
                return Roots.with().srcDirsOnly();
            }
            return scanRoots;
        }
        
		
		private static <T> Matcher<T> anyIfNull(Matcher<T> matcher){
			return Logical.anyIfNull(matcher);
		}
		
        private JAstParser getParserOrDefault(Roots.Builder roots,Roots.Builder scanRoots) {
            if (parser != null) {
                return parser;
            }
            return DefaultJAstParser.with()
                    .defaults()
                    .resourceLoader(Roots.with()
                        .roots(roots)
                        .roots(scanRoots)
                        .classpath(true)//include the current VM classpath (which we may not include in search)
                     )
                    .build();
        }

        public Builder roots(IBuilder<? extends Iterable<Root>> rootsBuilder) {
            roots(rootsBuilder.build());
            return this;
        }
        
        public Builder roots(Iterable<Root> roots) {
            this.roots = Roots.with().roots(roots);
            return this;
        }

	 	public Builder scanRoots(IBuilder<? extends Iterable<Root>> rootsBuilder) {
	 	   scanRoots(rootsBuilder.build());
        	return this;
        }
	 	
	 	public Builder scanRoots(Iterable<Root> roots) {
	 		this.scanRoots = Roots.with().roots(roots);
        	return this;
        }

	 	public Builder listener(MatchListener<Object> listener) {
        	this.listener = listener;
        	return this;
		}
	 	
	 	public Builder filter(SourceFilter.Builder filter) {
        	filter(filter.build());
        	return this;
		}
	 	
		public Builder filter(IBuilder<SourceMatcher> builder) {
        	filter(builder.build());
        	return this;
		}
		
		public Builder filter(SourceMatcher filters) {
			objectMatcher = filters.getObjectMatcher();
			rootMatcher = filters.getRootMatcher();
			resourceMatcher = filters.getResourceMatcher();			
			sourceMatcher = filters.getSourceMatcher();
			typeMatcher = filters.getTypeMatcher();
			methodMatcher = filters.getMethodMatcher();
			
        	return this;
		}

		public Builder parser(IBuilder<JAstParser> builder) {
        	parser(builder.build());
        	return this;
        }
		
		public Builder parser(JAstParser parser) {
        	this.parser = parser;
        	return this;
        }
		
		public Builder failOnParseError(boolean failOnParseError) {
            this.failOnParseError= failOnParseError;
            return this;
        }
	}
}
