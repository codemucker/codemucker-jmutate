package com.bertvanbrakel.codemucker.ast.finder;

import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder.JSourceFinderFilterCallback;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.google.common.base.Objects;

/**
 * Thread safe if underlying matchers are
 */
public class MatcherBackedFilter implements JSourceFinderFilterCallback {
	private final Matcher<Root> classPathMatcher;
	private final Matcher<String> resourceNameMatcher;
	private final Matcher<ClassPathResource> resourceMatcher;
	private final Matcher<String> classNameMatcher;
	private final Matcher<JSourceFile> sourceMatcher;
	private final Matcher<JType> typeMatcher;
	private final Matcher<JMethod> methodMatcher;
	
	public static Builder newBuilder(){
		return new Builder();
	}
	
	private MatcherBackedFilter(
			Matcher<Root> classPathMatcher
			, Matcher<String> resourceNameMatcher
			, Matcher<ClassPathResource> resourceMatcher
			, Matcher<String> classNameMatcher
			, Matcher<JSourceFile> sourceMatcher
			, Matcher<JType> typeMatcher
			, Matcher<JMethod> methodMatcher) {
        super();
        this.classPathMatcher = anyIfNull(classPathMatcher);
        this.resourceNameMatcher = anyIfNull(resourceNameMatcher);
        this.resourceMatcher = anyIfNull(resourceMatcher);
        this.classNameMatcher = anyIfNull(classNameMatcher);
        this.sourceMatcher = anyIfNull(sourceMatcher);
        this.typeMatcher = anyIfNull(typeMatcher);
        this.methodMatcher = anyIfNull(methodMatcher);
    }
	
	@SuppressWarnings("unchecked")
    private <T> Matcher<T> anyIfNull(Matcher<T> matcher){
		return (Matcher<T>) (matcher!=null?matcher:LogicalMatchers.any());
	}

	@Override
    public boolean matches(Object obj) {
        return true;
    }

	@Override
    public boolean matches(Root root) {
        return classPathMatcher.matches(root);
    }

	@Override
    public boolean matches(ClassPathResource resource) {
        return resourceNameMatcher.matches(resource.getRelPath()) && resourceMatcher.matches(resource);
    }

	@Override
    public boolean matches(JSourceFile file) {
        return classNameMatcher.matches(file.getClassnameBasedOnPath()) && sourceMatcher.matches(file);
    }

	@Override
    public boolean matches(JType type) {
        return typeMatcher.matches(type);
    }

	@Override
    public boolean matches(JMethod method) {
        return methodMatcher.matches(method);
    }
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
			.add("classPathMatcher", classPathMatcher)
			.add("resourceMatcher", resourceMatcher)
			.add("sourceMatcher", sourceMatcher)
			.add("typeMatcher", typeMatcher)
			.add("methodMatcher", methodMatcher)
			.toString();	
	}

	public static class Builder {
		private Matcher<Root> classPathMatcher;
		private Matcher<String> resourceNameMatcher;
		private Matcher<ClassPathResource> resourceMatcher;
		private Matcher<String> classNameMatcher;
		private Matcher<JSourceFile> sourceMatcher;
		private Matcher<JType> typeMatcher;
		private Matcher<JMethod> methodMatcher;
		
		public JSourceFinderFilterCallback build(){
			return new MatcherBackedFilter(
				classPathMatcher
				, resourceNameMatcher
				, resourceMatcher
				, classNameMatcher
				, sourceMatcher
				, typeMatcher
				, methodMatcher
			);	
		}
		
		public MatcherBackedFilter.Builder copyOf(){
			MatcherBackedFilter.Builder copy = new Builder();
			copy.classPathMatcher = classPathMatcher;
			copy.resourceNameMatcher = resourceNameMatcher;
			copy.resourceMatcher = resourceMatcher;
			copy.classNameMatcher = classNameMatcher;
			copy.sourceMatcher = sourceMatcher;
			copy.methodMatcher = methodMatcher;
			copy.typeMatcher = typeMatcher;
			return copy;
		}
		
		public MatcherBackedFilter.Builder setClassPathMatcher(Matcher<Root> classPathMatcher) {
        	this.classPathMatcher = classPathMatcher;
        	return this;
		}
		
		public MatcherBackedFilter.Builder setResourceNameMatcher(Matcher<String> resourceNameMatcher) {
        	this.resourceNameMatcher = resourceNameMatcher;
        	return this;
		}
				
		public MatcherBackedFilter.Builder setResourceMatcher(Matcher<ClassPathResource> resourceMatcher) {
        	this.resourceMatcher = resourceMatcher;
        	return this;
		}
		
		public MatcherBackedFilter.Builder setClassNameMatcher(Matcher<String> classNameMatcher) {
        	this.classNameMatcher = classNameMatcher;
        	return this;
		}
		
		public MatcherBackedFilter.Builder setSourceMatcher(Matcher<JSourceFile> sourceMatcher) {
        	this.sourceMatcher = sourceMatcher;
        	return this;
        }
		
		public MatcherBackedFilter.Builder setTypeMatcher(Matcher<JType> typeMatcher) {
        	this.typeMatcher = typeMatcher;
        	return this;
		}
		
		public MatcherBackedFilter.Builder setMethodMatcher(Matcher<JMethod> methodMatcher) {
        	this.methodMatcher = methodMatcher;
        	return this;
		}
	}
}