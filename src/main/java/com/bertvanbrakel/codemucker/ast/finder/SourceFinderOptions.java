package com.bertvanbrakel.codemucker.ast.finder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.JavaSourceFile;
import com.bertvanbrakel.codemucker.ast.finder.matcher.Matcher;
import com.bertvanbrakel.test.finder.FileMatcher;
import com.bertvanbrakel.test.finder.IncludeExcludeFileMatcher;
import com.bertvanbrakel.test.util.ProjectFinder;

public class SourceFinderOptions {
	
	private final Collection<String> projectFiles = new ArrayList<String>(ProjectFinder.DEF_PROJECT_FILES);
	
	private final Collection<File> classPathsDir = new HashSet<File>();
	
	private boolean includeClassesDir = true;
	private boolean includeTestDir = false;

	private final IncludeExcludeFileMatcher fileMatcher = new IncludeExcludeFileMatcher();
	private final IncludeExcludeMatcher<JavaSourceFile> sourceMatchers = new IncludeExcludeMatcher<JavaSourceFile>();
	private final IncludeExcludeMatcher<JType> typeMatchers = new IncludeExcludeMatcher<JType>();
	private final IncludeExcludeMatcher<JMethod> methodMatchers = new IncludeExcludeMatcher<JMethod>();
	
	public Collection<String> getProjectFiles() {
    	return projectFiles;
    }

	public SourceFinderOptions includeClassesDir(boolean b) {
		this.includeClassesDir = b;
		return this;
	}

	public SourceFinderOptions includeTestDir(boolean b) {
		this.includeTestDir = b;
		return this;
	}

	public Collection<File> getClassPathsDir() {
		return classPathsDir;
	}

	public boolean isIncludeClassesDir() {
		return includeClassesDir;
	}

	public boolean isIncludeTestDir() {
		return includeTestDir;
	}

	public SourceFinderOptions addClassPath(File dir) {
		classPathsDir.add(dir);
		return this;
	}

	public SourceFinderOptions includeFile(FileMatcher matcher) {
		this.fileMatcher.addInclude(matcher);
		return this;
	}
	
	public SourceFinderOptions excludeFile(FileMatcher matcher) {
		this.fileMatcher.addExclude(matcher);
		return this;
	}

	public SourceFinderOptions includeSource(Matcher<JavaSourceFile> matcher) {
		this.sourceMatchers.addInclude(matcher);
		return this;
	}
	
	public SourceFinderOptions excludeSource(Matcher<JavaSourceFile> matcher) {
		this.sourceMatchers.addExclude(matcher);
		return this;
	}

	public SourceFinderOptions includeMethods(Matcher<JMethod> matcher) {
		this.methodMatchers.addInclude(matcher);
		return this;
	}
	
	public SourceFinderOptions excludeMethods(Matcher<JMethod> matcher) {
		this.methodMatchers.addExclude(matcher);
		return this;
	}

	public SourceFinderOptions includeTypes(Matcher<JType> matcher) {
		this.typeMatchers.addInclude(matcher);
		return this;
	}
	
	public SourceFinderOptions excludeTypes(Matcher<JType> matcher) {
		this.typeMatchers.addExclude(matcher);
		return this;
	}

	public FileMatcher toFileMatcher() {
		return fileMatcher;
	}

	public Matcher<JavaSourceFile> toSourceMatcher() {
		return sourceMatchers;
	}
	
	public Matcher<JType> toTypeMatcher() {
		return typeMatchers;
	}
	
	public Matcher<JMethod> toMethodMatcher(){
		return methodMatchers;
	}
}