package org.codemucker.jmutate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.codemucker.jfind.BaseRootVisitor;
import org.codemucker.jfind.DefaultFindResult;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.JFindMatchListener;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;
import org.codemucker.jfind.RootVisitor;
import org.codemucker.jfind.Roots;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JFindVisitor;
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
public class JMutateFinder {

	private static final String JAVA_EXTENSION = "java";
	private static final JFindMatchListener<Object> NULL_LISTENER = new JFindMatchListener<Object>() {
		@Override
		public void onMatched(Object obj) {
		}
		
		@Override
		public void onIgnored(Object obj) {
		}

		@Override
		public void onError(Object record, Exception e) throws Exception {
		}
	};
	
	private final Collection<Root> roots;

	private final Matcher<Root> rootMatcher;
	private final Matcher<RootResource> resourceMatcher;
	private final Matcher<JSourceFile> sourceMatcher;
	private final Matcher<JType> typeMatcher;
	private final Matcher<JMethod> methodMatcher;
	
	private final JFindMatchListener<Object> listener;
	
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

	@Inject
	public JMutateFinder(
			JAstParser parser
			, Iterable<Root> classPathRoots
			, SourceMatcher matchers
			, JFindMatchListener<Object> listener
			) {
		this(parser,
			classPathRoots,
			matchers.getObjectMatcher(),
			matchers.getRootMatcher(),
			matchers.getResourceMatcher(),			
			matchers.getSourceMatcher(),
			matchers.getTypeMatcher(),
			matchers.getMethodMatcher(),
			listener
		);
	}

	public JMutateFinder(
			JAstParser parser
			, Iterable<Root> roots
			, Matcher<Object> objectFilter
			, Matcher<Root> rootFilter
			, Matcher<RootResource> resourceFilter
			, Matcher<JSourceFile> sourceFilter
			, Matcher<JType> typeFilter
			, Matcher<JMethod> methodFilter
			, JFindMatchListener<Object> listener
			) {
		
		checkNotNull(roots, "expect class path roots");
		
		this.parser = checkNotNull(parser, "expect parser");
		this.roots = ImmutableList.<Root>builder().addAll(roots).build();

		this.rootMatcher = join(checkNotNull(rootFilter, "expect root filter"), objectFilter);
		this.resourceMatcher = join(checkNotNull(resourceFilter, "expect resource filter"), objectFilter);
		this.sourceMatcher = join(checkNotNull(sourceFilter, "expect source filter"), objectFilter);
		this.typeMatcher = join(checkNotNull(typeFilter, "expect type filter"), objectFilter);
		this.methodMatcher = join(checkNotNull(methodFilter, "expect method filter"), objectFilter);
	
		this.listener = checkNotNull(listener, "expect find listener");
	}
	
	private static <T> Matcher<T> join(final Matcher<T> matcher,final Matcher<Object> objMatcher){
		return new AbstractNotNullMatcher<T>(){
			@Override
			public boolean matchesSafely(T found, MatchDiagnostics diag) {
				return objMatcher.matches(found) && matcher.matches(found);
			}
		};
	}
	public void visit(JFindVisitor visitor) {
		for (JSourceFile srcFile : findSources()) {
			srcFile.visit(visitor);
		}
	}
	
	public FindResult<JMethod> findMethods() {
		return findTypes().transformToMany(typeToMethods()).filter(methodMatcher, listener);
	}
	
	private Function<JType, Iterator<JMethod>> typeToMethods(){
		return new Function<JType,Iterator<JMethod>>(){
			public Iterator<JMethod> apply(JType type){
				return type.findMethods().iterator();
			}
		};
	}

	public FindResult<JType> findTypes() {
		return findSources().transformToMany(sourceToTypes()).filter(typeMatcher, listener);
    }
		
	private Function<JSourceFile, Iterator<JType>> sourceToTypes(){
		return new Function<JSourceFile,Iterator<JType>>(){
			public Iterator<JType> apply(JSourceFile source){
				return source.findAllTypes().iterator();
			}
		};
	}
	
	public FindResult<JSourceFile> findSources() {
		return findResources().transform(resourceToSource()).filter(sourceMatcher, listener);
	}
	
	private Function<RootResource, JSourceFile> resourceToSource(){
		return new Function<RootResource,JSourceFile>(){
			public JSourceFile apply(RootResource resource){
				if( resource.hasExtension(JAVA_EXTENSION)){
					//TODO:catch error here and callback onerror?
					return JSourceFile.fromResource(resource, parser);
				}
				//indicate skip this item
				return null;
			}
		};
	}
	
	public FindResult<RootResource> findResources(){
		final List<RootResource> resources = Lists.newArrayListWithExpectedSize(200);	
		RootVisitor visitor = new BaseRootVisitor(){
			@Override
			public boolean visit(Root root) {
				boolean visit = rootMatcher.matches(root);
				if(visit){
					listener.onMatched(root);
				} else {
					listener.onIgnored(root);	
				}
				return visit;
			}
			@Override
			public boolean visit(RootResource resource) {
				boolean visit = resourceMatcher.matches(resource);
				if(visit){
					resources.add(resource);
					listener.onMatched(resource);
				} else {
					listener.onIgnored(resource);	
				}
				return visit;
			}
		};

		for(Root root:roots){
			root.accept(visitor);
		}

		return DefaultFindResult.from(resources);
	}
	
	public FindResult<Root> findRoots() {
		return DefaultFindResult.from(roots).filter(rootMatcher);
	}
	
	public static class Builder {
		private JAstParser parser;
		private List<Root> roots = newArrayList();
		
		private Matcher<Object> objectMatcher;
		private Matcher<Root> rootMatcher;
		private Matcher<RootResource> resourceMatcher;
		private Matcher<JSourceFile> sourceMatcher;
		private Matcher<JType> typeMatcher;
		private Matcher<JMethod> methodMatcher;
		
		private JFindMatchListener<Object> listener;
		
		public JMutateFinder build(){
		    JAstParser parser = buildParser();
			return new JMutateFinder(
			     parser
				, roots
				, anyIfNull(objectMatcher)
				, anyIfNull(rootMatcher)
				, anyIfNull(resourceMatcher)
				, anyIfNull(sourceMatcher)
				, anyIfNull(typeMatcher)
				, anyIfNull(methodMatcher)
				, listener==null?NULL_LISTENER:listener
			);
		}

		private static <T> Matcher<T> anyIfNull(Matcher<T> matcher){
			return Logical.anyIfNull(matcher);
		}
		
        private JAstParser buildParser() {
            if (parser != null) {
                return parser;
            }
            return JAstParser.with()
                    .defaults()
                    .roots(roots)//path to all the code we're searching
                    .addRoots(Roots.with().classpath(true))//include the current VM classpath (which we may not include in search)
                    .build();
        }

		public Builder searchRoots(Roots.Builder searchRoots) {
        	searchRoots(searchRoots.build());
        	return this;
        }
		
	 	public Builder searchRoots(IBuilder<? extends Iterable<Root>> rootsBuilder) {
        	searchRoots(rootsBuilder.build());
        	return this;
        }
	 	
	 	public Builder searchRoots(Iterable<Root> roots) {
	 		for(Root r:roots){
	 			searchRoot(r);
	 		}
        	return this;
        }
	 	
	 	public Builder searchRoot(Root root) {
        	this.roots.add(root);
        	return this;
        }

	 	public Builder listener(JFindMatchListener<Object> listener) {
        	this.listener = listener;
        	return this;
		}
	 	
	 	public Builder filter(JMutateFilter.Builder filter) {
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
	}
}
