package com.bertvanbrakel.codemucker.ast.finder;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder.JSourceFinderFilterCallback;
import com.bertvanbrakel.codemucker.ast.matcher.ASourceFile;
import com.bertvanbrakel.codemucker.ast.matcher.AType;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.finder.matcher.IncludeExcludeMatcherBuilder;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.bertvanbrakel.test.finder.matcher.ResourceMatchers;

public class FilterBuilder {

	private IncludeExcludeMatcherBuilder<Root> roots = IncludeExcludeMatcherBuilder.newBuilder();
	private IncludeExcludeMatcherBuilder<String> resourceNames = IncludeExcludeMatcherBuilder.newBuilder();
	private IncludeExcludeMatcherBuilder<ClassPathResource> resources = IncludeExcludeMatcherBuilder.newBuilder();	
	private IncludeExcludeMatcherBuilder<String> classNames = IncludeExcludeMatcherBuilder.newBuilder();
	private IncludeExcludeMatcherBuilder<JSourceFile> sources = IncludeExcludeMatcherBuilder.newBuilder();
	private IncludeExcludeMatcherBuilder<JMethod> methods = IncludeExcludeMatcherBuilder.newBuilder();
	private IncludeExcludeMatcherBuilder<JType> types = IncludeExcludeMatcherBuilder.newBuilder();
	
	public static FilterBuilder newBuilder(){
		return new FilterBuilder();
	}
	
	private FilterBuilder(){
		//prevent instantiation outside of builder method
	}
	
	public JSourceFinderFilterCallback build(){
		JSourceFinderFilterCallback filter = MatcherBackedFilter.newBuilder()
			.setClassPathMatcher(roots.build())
			.setResourceMatcher(resources.build())
			.setResourceNameMatcher(resourceNames.build())
			.setClassNameMatcher(classNames.build())
			.setSourceMatcher(sources.build())
			.setMethodMatcher(methods.build())
			.setTypeMatcher(types.build())
			.build();
		return filter;
	}
	
	public FilterBuilder copyOf(){
		FilterBuilder copy = new FilterBuilder();
		copy.roots = roots.copyOf();
		copy.resourceNames = resourceNames.copyOf();
		copy.resources = resources.copyOf();
		copy.sources = sources.copyOf();
		copy.methods = methods.copyOf();
		copy.types = types.copyOf();
		return copy;
	}

	public FilterBuilder setIncludeFileName(String pattern) {
		setIncludeResource(ResourceMatchers.withAntPath(pattern));
		return this;
	}
	
	public FilterBuilder setIncludeFileName(Pattern pattern) {
		setIncludeResource(ResourceMatchers.withPath(pattern));
		return this;
	}
	
	public FilterBuilder setIncludeResource(Matcher<ClassPathResource> matcher) {
		resources.addInclude(matcher);
		return this;
	}

	public FilterBuilder setExcludeFileName(String path) {
		setExcludeResource(ResourceMatchers.withAntPath(path));
		return this;
	}
	
	public FilterBuilder setExcludeFileName(Pattern pattern) {
		setExcludeResource(ResourceMatchers.withPath(pattern));
		return this;
	}

	public FilterBuilder setExcludeResource(Matcher<ClassPathResource> matcher) {
		resources.addExclude(matcher);
		return this;
	}

	public FilterBuilder setAssignableTo(Class<?> superclass) {
		setIncludeSource(ASourceFile.assignableTo(superclass));
		return this;
	}
	
	public <T extends Annotation> FilterBuilder withAnnotation(Class<T> annotation){
		setIncludeSource(ASourceFile.withAnnotation(annotation));
		return this;
	}
	
	public FilterBuilder setIncludeSource(Matcher<JSourceFile> matcher) {
		sources.addInclude(matcher);
		return this;
	}
	
	public FilterBuilder setExcludeEnum() {
		setExcludeSource(ASourceFile.includeEnum());
		return this;
	}

	public FilterBuilder setExcludeAnonymous() {
		setExcludeSource(ASourceFile.includeAnonymous());
		return this;
	}

	public FilterBuilder setExcludeInterfaces() {
		setExcludeSource(ASourceFile.includeInterfaces());
		return this;
	}

	public FilterBuilder setExcludeSource(Matcher<JSourceFile> matcher) {
		sources.addExclude(matcher);
		return this;
	}
	
	public FilterBuilder addIncludeTypesWithMethods(Matcher<JMethod> matcher){
		addIncludeTypes(AType.withMethod(matcher));
		return this;
	}
	
	public FilterBuilder addExcludeTypesWithMethods(Matcher<JMethod> matcher){
		addExcludeTypes(AType.withMethod(matcher));
		return this;
	}
	
	public FilterBuilder addIncludeTypes(Matcher<JType> matcher){
		types.addInclude(matcher);
		return this;
	}
	
	public FilterBuilder addExcludeTypes(Matcher<JType> matcher){
		types.addExclude(matcher);
		return this;
	}
	
	public FilterBuilder addIncludeMethods(Matcher<JMethod> matcher){
		methods.addInclude(matcher);
		return this;
	}
	
	public FilterBuilder addExcludeMethods(Matcher<JMethod> matcher){
		methods.addExclude(matcher);
		return this;
	}
	
}