package com.bertvanbrakel.codemucker.ast.finder;

import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder.JFindMatcher;
import com.bertvanbrakel.codemucker.ast.matcher.ASourceFile;
import com.bertvanbrakel.codemucker.ast.matcher.AType;
import com.bertvanbrakel.lang.IsBuilder;
import com.bertvanbrakel.test.finder.RootResource;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.finder.matcher.IncludeExcludeMatcherBuilder;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.bertvanbrakel.test.finder.matcher.ResourceMatchers;

public class Filter implements JFindMatcher {

	private final FindResult.Filter<Object> objectFilter;
	private final FindResult.Filter<Root> rootFilter;
	private final FindResult.Filter<RootResource> resourceFilter;
	private final FindResult.Filter<JSourceFile> sourceFilter;
	private final FindResult.Filter<JType> typeMatcher;
	private final FindResult.Filter<JMethod> methodFilter;
	
	private Filter(
			FindResult.Filter<Object> objectFilter
			, FindResult.Filter<Root> rootFilter
			, FindResult.Filter<RootResource> resourceMatcher
			, FindResult.Filter<JSourceFile> sourceMatcher
			, FindResult.Filter<JType> typeMatcher
			, FindResult.Filter<JMethod> methodMatcher) {
        super();
        
        this.objectFilter = objectFilter;
        this.rootFilter = rootFilter;
        this.resourceFilter = resourceMatcher;
        this.sourceFilter = sourceMatcher;
        this.typeMatcher = typeMatcher;
        this.methodFilter = methodMatcher;
    }
	
	@Override
	public FindResult.Filter<Object> getObjectMatcher() {
		return objectFilter;
	}

	@Override
	public FindResult.Filter<Root> getRootMatcher() {
		return rootFilter;
	}

	@Override 
	public FindResult.Filter<RootResource> getResourceMatcher() {
		return resourceFilter;
	}

	@Override
	public FindResult.Filter<JSourceFile> getSourceMatcher() {
		return sourceFilter;
	}

	@Override
	public FindResult.Filter<JType> getTypeMatcher() {
		return typeMatcher;
	}

	@Override
	public FindResult.Filter<JMethod> getMethodMatcher() {
		return methodFilter;
	}	
	
	public static Builder builder(){
		return new Builder();
	}

	public static class Builder implements IsBuilder<JFindMatcher> {
		
		private FindResult.Filter<Object> ANY = new FindResult.Filter<Object>(){

			@Override
			public boolean matches(Object found) {
				return true;
			}

			@Override
			public void onMatched(Object result) {}

			@Override
			public void onIgnored(Object result) {}
	    };
	    
	    private IncludeExcludeMatcherBuilder<Root> roots = IncludeExcludeMatcherBuilder.builder();
		private IncludeExcludeMatcherBuilder<String> resourceNames = IncludeExcludeMatcherBuilder.builder();
		private IncludeExcludeMatcherBuilder<RootResource> resources = IncludeExcludeMatcherBuilder.builder();	
		private IncludeExcludeMatcherBuilder<String> classNames = IncludeExcludeMatcherBuilder.builder();
		private IncludeExcludeMatcherBuilder<JSourceFile> sources = IncludeExcludeMatcherBuilder.builder();
		private IncludeExcludeMatcherBuilder<JMethod> methods = IncludeExcludeMatcherBuilder.builder();
		private IncludeExcludeMatcherBuilder<JType> types = IncludeExcludeMatcherBuilder.builder();

		private Callable<Object> matchListener;
		
		private Builder(){
			//prevent instantiation outside of builder method
		}
		
		public JFindMatcher build(){
			return new Filter(
				 ANY	
				, toFilter(roots.build())
				, toFilter(mergeResourceMatchers(resources.build(),resourceNames.build()))
				, toFilter(mergeSourceMatchers(sources.build(),classNames.build()))
				, toFilter(types.build())
				, toFilter(methods.build())
			);	
		}
			
		private <T> FindResult.Filter<T> toFilter(Matcher<T> matcher){
			return MatcherToFilterAdapter.from(matcher);
		}
		
		private static Matcher<RootResource> mergeResourceMatchers(final Matcher<RootResource> matcher, final Matcher<String> resourceNameMatcher){
			return new Matcher<RootResource>(){
				@Override
				public boolean matches(RootResource found) {
					return resourceNameMatcher.matches(found.getRelPath()) && matcher.matches(found);
				}
			};
		}
		
		private static Matcher<JSourceFile> mergeSourceMatchers(final Matcher<JSourceFile> matcher, final Matcher<String> classNameMatcher){
			return new Matcher<JSourceFile>(){
				@Override
				public boolean matches(JSourceFile found) {
					return classNameMatcher.matches(found.getClassnameBasedOnPath()) && matcher.matches(found);
				}
			};
		}
	
		public Builder setMatchListener(Callable<Object> matchListener) {
			this.matchListener = matchListener;
			return this;
		}
		
		public Builder setIncludeFileName(String pattern) {
			setIncludeResource(ResourceMatchers.withAntPath(pattern));
			return this;
		}
		
		public Builder setIncludeFileName(Pattern pattern) {
			setIncludeResource(ResourceMatchers.withPath(pattern));
			return this;
		}
		
		public Builder setIncludeResource(Matcher<RootResource> matcher) {
			resources.addInclude(matcher);
			return this;
		}
	
		public Builder setExcludeFileName(String path) {
			setExcludeResource(ResourceMatchers.withAntPath(path));
			return this;
		}
		
		public Builder setExcludeFileName(Pattern pattern) {
			setExcludeResource(ResourceMatchers.withPath(pattern));
			return this;
		}
	
		public Builder setExcludeResource(Matcher<RootResource> matcher) {
			resources.addExclude(matcher);
			return this;
		}
	
		public Builder setAssignableTo(Class<?> superclass) {
			setIncludeSource(ASourceFile.assignableTo(superclass));
			return this;
		}
		
		public <T extends Annotation> Builder withAnnotation(Class<T> annotation){
			setIncludeSource(ASourceFile.withAnnotation(annotation));
			return this;
		}
		
		public Builder setIncludeSource(Matcher<JSourceFile> matcher) {
			sources.addInclude(matcher);
			return this;
		}
		
		public Builder setExcludeEnum() {
			setExcludeSource(ASourceFile.includeEnum());
			return this;
		}
	
		public Builder setExcludeAnonymous() {
			setExcludeSource(ASourceFile.includeAnonymous());
			return this;
		}
	
		public Builder setExcludeInterfaces() {
			setExcludeSource(ASourceFile.includeInterfaces());
			return this;
		}
	
		public Builder setExcludeSource(Matcher<JSourceFile> matcher) {
			sources.addExclude(matcher);
			return this;
		}
		
		public Builder addIncludeTypesWithMethods(Matcher<JMethod> matcher){
			addIncludeTypes(AType.withMethod(matcher));
			return this;
		}
		
		public Builder addExcludeTypesWithMethods(Matcher<JMethod> matcher){
			addExcludeTypes(AType.withMethod(matcher));
			return this;
		}
		
		public Builder addIncludeTypes(Matcher<JType> matcher){
			types.addInclude(matcher);
			return this;
		}
		
		public Builder addExcludeTypes(Matcher<JType> matcher){
			types.addExclude(matcher);
			return this;
		}
		
		public Builder addIncludeMethods(Matcher<JMethod> matcher){
			methods.addInclude(matcher);
			return this;
		}
		
		public Builder addExcludeMethods(Matcher<JMethod> matcher){
			methods.addExclude(matcher);
			return this;
		}
	}
	
}