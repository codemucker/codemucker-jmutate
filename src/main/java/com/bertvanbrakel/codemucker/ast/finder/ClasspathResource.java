package com.bertvanbrakel.codemucker.ast.finder;

import java.io.File;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ClasspathResource {
	private File classDir;
	private String relativePath;
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

	public String getPackagePart(){
		int slash = relativePath.lastIndexOf('/');
		if( slash != -1){
			return relativePath.substring(0, slash).replace('/', '.');
		}
		return null;
	}
	
	public String getFilenamePart(){
		int slash = relativePath.lastIndexOf('/');
		int dot = relativePath.lastIndexOf('.');
		if( slash != -1){
			return relativePath.substring(slash + 1, dot);
		}
		
		return relativePath.substring(0, dot);
	}
	
	public String getPathWithoutExtension(){
		if( extension != null ){
			return relativePath.substring(0, relativePath.length() - extension.length());
		}
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
