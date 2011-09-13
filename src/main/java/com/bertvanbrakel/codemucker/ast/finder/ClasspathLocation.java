package com.bertvanbrakel.codemucker.ast.finder;

import java.io.File;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.bertvanbrakel.test.bean.ClassUtils;

public class ClasspathLocation {
	private String relativePath;
	private File classDir;
	private String pathBasedClassName;

	public ClasspathLocation(File classDir, String relativePath) {
		super();
		this.classDir = classDir;
		this.relativePath = relativePath;
		this.pathBasedClassName = ClassUtils.pathToClassName(relativePath);
	}

	public String getRelativrPath() {
		return relativePath;
	}

	public File getFile() {
		return new File(classDir, relativePath);
	}

	public String getClassname() {
		return pathBasedClassName;
	}

	public File getClassDir() {
		return classDir;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
