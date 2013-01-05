package com.bertvanbrakel.codemucker.ast.finder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;

import com.bertvanbrakel.codemucker.ast.JAstParser;
import com.bertvanbrakel.codemucker.ast.JFindVisitor;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.lang.IsBuilder;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;
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
public class JSourceFinder {

	private static final String JAVA_EXTENSION = "java";
	private static final JFindListener NULL_LISTENER = new JFindListener() {
		@Override
		public void onMatched(Object obj) {
		}
		
		@Override
		public void onIgnored(Object obj) {
		}
	};
	
	private final Collection<Root> classPathRoots;

	private final Matcher<Root> rootMatcher;
	private final Matcher<ClassPathResource> resourceFilter;
	private final Matcher<JSourceFile> sourceMatcher;
	private final Matcher<JType> typeMatcher;
	private final Matcher<JMethod> methodMatcher;
	
	private final JFindListener listener;
	
	@Inject
	private final ASTParser parser;

	public static interface JFindMatcher {
		public Matcher<Object> getObjectMatcher();
		public Matcher<Root> getRootMatcher();
		public Matcher<ClassPathResource> getResourceMatcher();
		public Matcher<JSourceFile> getSourceMatcher();
		public Matcher<JType> getTypeMatcher();
		public Matcher<JMethod> getMethodMatcher();
	}
	
	public static interface JFindListener extends FindResult.MatchListener<Object> {
	}
	
	public static Builder builder(){
		return new Builder();
	}

	@Inject
	public JSourceFinder(
			ASTParser parser
			, Iterable<Root> classPathRoots
			, JFindMatcher matchers
			, JFindListener listener
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

	public JSourceFinder(
			ASTParser parser
			, Iterable<Root> roots
			, Matcher<Object> objectFilter
			, Matcher<Root> rootFilter
			, Matcher<ClassPathResource> resourceFilter
			, Matcher<JSourceFile> sourceFilter
			, Matcher<JType> typeFilter
			, Matcher<JMethod> methodFilter
			, JFindListener listener
			) {
		
		checkNotNull(roots, "expect class path roots");
		
		this.parser = checkNotNull(parser, "expect parser");
		this.classPathRoots = ImmutableList.<Root>builder().addAll(roots).build();

		this.rootMatcher = join(checkNotNull(rootFilter, "expect root filter"), objectFilter);
		this.resourceFilter = join(checkNotNull(resourceFilter, "expect resource filter"), objectFilter);
		this.sourceMatcher = join(checkNotNull(sourceFilter, "expect source filter"), objectFilter);
		this.typeMatcher = join(checkNotNull(typeFilter, "expect type filter"), objectFilter);
		this.methodMatcher = join(checkNotNull(methodFilter, "expect method filter"), objectFilter);
	
		this.listener = checkNotNull(listener, "expect find listener");
	}
	
	private static <T> Matcher<T> join(final Matcher<T> matcher,final Matcher<Object> objMatcher){
		return new Matcher<T>(){
			@Override
			public boolean matches(T found) {
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
				return type.findAllJMethods().iterator();
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
	
	private Function<ClassPathResource, JSourceFile> resourceToSource(){
		return new Function<ClassPathResource,JSourceFile>(){
			public JSourceFile apply(ClassPathResource resource){
				if( resource.hasExtension(JAVA_EXTENSION)){
					//TODO:catch error here and callback onerror?
					return JSourceFile.fromResource(resource, parser);
				}
				//indicate skip this item
				return null;
			}
		};
	}
	
	public FindResult<ClassPathResource> findResources(){
		return findRoots().transformToMany(rootToResources()).filter(resourceFilter, listener);
	}
	
	private Function<Root, Iterator<ClassPathResource>> rootToResources(){
		return new Function<Root,Iterator<ClassPathResource>>(){
			public Iterator<ClassPathResource> apply(Root root){
				Collection<ClassPathResource> resources = Lists.newArrayList();
				collectResources(root, resources);
				return resources.iterator();
			}
		};
	}
	
	private void collectResources(Root root,final Collection<ClassPathResource> found){
		Function<ClassPathResource, Boolean> collector = new Function<ClassPathResource, Boolean>() {
			@Override
            public Boolean apply(ClassPathResource child) {
				if (resourceFilter.matches(child)) {
					listener.onMatched((Object)child);
					found.add(child);
				} else {
					listener.onIgnored((Object)child);
				}
				return true;
            }
		};
		root.walkResources(collector);
	}
	
	public FindResult<Root> findRoots() {
		return FindResultImpl.from(classPathRoots).filter(rootMatcher);
	}
	
	public static class Builder {
		private ASTParser parser;
		private List<Root> roots = newArrayList();
		
		private Matcher<Object> objectFilter;
		private Matcher<Root> rootFilter;
		private Matcher<ClassPathResource> resourceFilter;
		private Matcher<JSourceFile> sourceFilter;
		private Matcher<JType> typeFilter;
		private Matcher<JMethod> methodFilter;
		
		private JFindListener listener;
		
		public JSourceFinder build(){			
			return new JSourceFinder(
				toParser()
				, roots
				, anyIfNull(objectFilter)
				, anyIfNull(rootFilter)
				, anyIfNull(resourceFilter)
				, anyIfNull(sourceFilter)
				, anyIfNull(typeFilter)
				, anyIfNull(methodFilter)
				, listener==null?NULL_LISTENER:listener
			);
		}

		private static <T> Matcher<T> anyIfNull(Matcher<T> matcher){
			return LogicalMatchers.anyIfNull(matcher);
		}
		
		private ASTParser toParser(){
			return parser != null ? parser : JAstParser.newDefaultParser();
		}

		public Builder setSearchRoots(SearchRoots.Builder searchRoots) {
        	setSearchRoots(searchRoots.build());
        	return this;
        }
		
	 	public Builder setSearchRoots(IsBuilder<? extends Iterable<Root>> rootsBuilder) {
        	setSearchRoots(rootsBuilder.build());
        	return this;
        }
	 	
	 	public Builder setSearchRoots(Iterable<Root> roots) {
        	this.roots = nullSafeList(roots);
        	return this;
        }
	 	
	 	private static <T> List<T> nullSafeList(Iterable<T> iter){
	 		if( iter == null){
	 			return newArrayList();
	 		}
	 		return newArrayList(iter);
	 	}

	 	public Builder setListener(JFindListener listener) {
        	this.listener = listener;
        	return this;
		}
	 	
	 	public Builder setFilter(Filter.Builder filter) {
        	setFilter(filter.build());
        	return this;
		}
	 	
		public Builder setFilter(IsBuilder<JFindMatcher> builder) {
        	setFilter(builder.build());
        	return this;
		}
		
		public Builder setFilter(JFindMatcher filters) {
			objectFilter = filters.getObjectMatcher();
			rootFilter = filters.getRootMatcher();
			resourceFilter = filters.getResourceMatcher();			
			sourceFilter = filters.getSourceMatcher();
			typeFilter = filters.getTypeMatcher();
			methodFilter = filters.getMethodMatcher();
			
        	return this;
		}

		public Builder setParser(ASTParser parser) {
        	this.parser = parser;
        	return this;
        }
	}
}
