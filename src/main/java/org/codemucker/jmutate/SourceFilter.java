package org.codemucker.jmutate;

import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.MatcherToFindFilterAdapter;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;
import org.codemucker.jfind.matcher.IncludeExcludeMatcherBuilder;
import org.codemucker.jfind.matcher.ResourceMatchers;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.SourceFinder.SourceMatcher;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJSourceFile;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.lang.IBuilder;


public class SourceFilter implements SourceMatcher {

	private final FindResult.Filter<Object> objectFilter;
	private final FindResult.Filter<Root> rootFilter;
	private final FindResult.Filter<RootResource> resourceFilter;
	private final FindResult.Filter<JSourceFile> sourceFilter;
	private final FindResult.Filter<JType> typeMatcher;
	private final FindResult.Filter<JMethod> methodFilter;
	
	private SourceFilter(
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

	public static class Builder implements IBuilder<SourceMatcher> {
		
		private FindResult.Filter<Object> ANY = new FindResult.Filter<Object>(){

			@Override
			public boolean matches(Object found) {
				return true;
			}

			@Override
			public boolean matches(Object actual, MatchDiagnostics ctxt) {
				return true;
			}
			
			@Override
			public void onMatched(Object result) {}

			@Override
			public void onIgnored(Object result) {}

			@Override
			public void describeTo(Description desc) {
				desc.text("anything");
			}
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
		
		public SourceMatcher build(){
			return new SourceFilter(
				 ANY	
				, toFilter(roots.build())
				, toFilter(mergeResourceMatchers(resources.build(),resourceNames.build()))
				, toFilter(mergeSourceMatchers(sources.build(),classNames.build()))
				, toFilter(types.build())
				, toFilter(methods.build())
			);	
		}
			
		private <T> FindResult.Filter<T> toFilter(Matcher<T> matcher){
			return MatcherToFindFilterAdapter.from(matcher);
		}
		
		private static Matcher<RootResource> mergeResourceMatchers(final Matcher<RootResource> matcher, final Matcher<String> resourceNameMatcher){
			return new AbstractNotNullMatcher<RootResource>(){
				@Override
				public boolean matchesSafely(RootResource found, MatchDiagnostics diag) {
					return resourceNameMatcher.matches(found.getRelPath()) && matcher.matches(found);
				}
			};
		}
		
		private static Matcher<JSourceFile> mergeSourceMatchers(final Matcher<JSourceFile> matcher, final Matcher<String> classNameMatcher){
			return new AbstractNotNullMatcher<JSourceFile>(){
				@Override
				public boolean matchesSafely(JSourceFile found, MatchDiagnostics diag) {
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
			setIncludeSource(AJSourceFile.assignableTo(superclass));
			return this;
		}
		
		public <T extends Annotation> Builder withAnnotation(Class<T> annotation){
			setIncludeSource(AJSourceFile.withAnnotation(annotation));
			return this;
		}
		
		public Builder setIncludeSource(Matcher<JSourceFile> matcher) {
			sources.addInclude(matcher);
			return this;
		}
		
		public Builder setExcludeEnum() {
			setExcludeSource(AJSourceFile.includeEnum());
			return this;
		}
	
		public Builder setExcludeAnonymous() {
			setExcludeSource(AJSourceFile.includeAnonymous());
			return this;
		}
	
		public Builder setExcludeInterfaces() {
			setExcludeSource(AJSourceFile.includeInterfaces());
			return this;
		}
	
		public Builder setExcludeSource(Matcher<JSourceFile> matcher) {
			sources.addExclude(matcher);
			return this;
		}
		
		public Builder addIncludeTypesWithMethods(Matcher<JMethod> matcher){
			addIncludeTypes(AJType.withMethod(matcher));
			return this;
		}
		
		public Builder addExcludeTypesWithMethods(Matcher<JMethod> matcher){
			addExcludeTypes(AJType.withMethod(matcher));
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