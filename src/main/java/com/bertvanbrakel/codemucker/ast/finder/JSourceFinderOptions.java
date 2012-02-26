package com.bertvanbrakel.codemucker.ast.finder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.finder.matcher.Matcher;
import com.bertvanbrakel.test.finder.FileMatcher;
import com.bertvanbrakel.test.finder.IncludeExcludeFileMatcher;
import com.bertvanbrakel.test.util.ProjectFinder;

public class JSourceFinderOptions {
	
	private final Collection<String> projectFiles = new ArrayList<String>(ProjectFinder.DEF_PROJECT_FILES);
	
	private final Collection<File> classPathsDir = new HashSet<File>();
	
	private boolean includeClassesDir = true;
	private boolean includeTestDir = false;

	private final IncludeExcludeFileMatcher fileMatcher = new IncludeExcludeFileMatcher();
	private final IncludeExcludeMatcher<JSourceFile> sourceMatchers = new IncludeExcludeMatcher<JSourceFile>();
	private final IncludeExcludeMatcher<JType> typeMatchers = new IncludeExcludeMatcher<JType>();
	private final IncludeExcludeMatcher<JMethod> methodMatchers = new IncludeExcludeMatcher<JMethod>();
	
	public Collection<String> getProjectFiles() {
    	return projectFiles;
    }

	public JSourceFinderOptions includeClassesDir(boolean b) {
		this.includeClassesDir = b;
		return this;
	}

	public JSourceFinderOptions includeTestDir(boolean b) {
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

	public JSourceFinderOptions addClassPath(File dir) {
		classPathsDir.add(dir);
		return this;
	}

	public JSourceFinderOptions includeFile(FileMatcher matcher) {
		this.fileMatcher.addInclude(matcher);
		return this;
	}
	
	public JSourceFinderOptions excludeFile(FileMatcher matcher) {
		this.fileMatcher.addExclude(matcher);
		return this;
	}

	public JSourceFinderOptions includeSource(Matcher<JSourceFile> matcher) {
		this.sourceMatchers.addInclude(matcher);
		return this;
	}
	
	public JSourceFinderOptions excludeSource(Matcher<JSourceFile> matcher) {
		this.sourceMatchers.addExclude(matcher);
		return this;
	}

	public JSourceFinderOptions includeMethods(Matcher<JMethod> matcher) {
		this.methodMatchers.addInclude(matcher);
		return this;
	}
	
	public JSourceFinderOptions excludeMethods(Matcher<JMethod> matcher) {
		this.methodMatchers.addExclude(matcher);
		return this;
	}

	public JSourceFinderOptions includeTypes(Matcher<JType> matcher) {
		this.typeMatchers.addInclude(matcher);
		return this;
	}
	
	public JSourceFinderOptions excludeTypes(Matcher<JType> matcher) {
		this.typeMatchers.addExclude(matcher);
		return this;
	}

	public FileMatcher toFileMatcher() {
		return fileMatcher;
	}

	public Matcher<JSourceFile> toSourceMatcher() {
		return sourceMatchers;
	}
	
	public Matcher<JType> toTypeMatcher() {
		return typeMatchers;
	}
	
	public Matcher<JMethod> toMethodMatcher(){
		return methodMatchers;
	}
}