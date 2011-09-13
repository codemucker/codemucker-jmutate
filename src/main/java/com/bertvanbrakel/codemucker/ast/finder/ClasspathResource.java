package com.bertvanbrakel.codemucker.ast.finder;

import java.io.File;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ClasspathResource {
	private String relativePath;
	private File classDir;
	private String extension;

	public ClasspathResource(File classDir, String relativePath) {
		super();
		this.classDir = classDir;
		this.relativePath = relativePath;
		this.extension = extractExtension(relativePath);
	}

	private static String extractExtension(String relativePath){
		int dot = relativePath.lastIndexOf('.');
		if( dot != -1){
			return relativePath.substring(dot + 1);
		}
		return null;
	}
	
	public boolean isExtension(String extension){
		if( extension == null ){
			return this.extension == null;
		}
		return this.extension != null && this.extension.equals(extension);
	}

	public String getRelativePath() {
		return relativePath;
	}

	public File getFile() {
		return new File(classDir, relativePath);
	}

	public String getExtension(){
		return extension;
	}

	public File getClassDir() {
		return classDir;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
