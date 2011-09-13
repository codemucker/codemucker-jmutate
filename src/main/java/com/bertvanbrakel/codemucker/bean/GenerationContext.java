package com.bertvanbrakel.codemucker.bean;

import java.io.File;

import com.bertvanbrakel.test.util.ProjectFinder;

public class GenerationContext {

	private File srcMainDir;
	private File srcTestDir;
	private File generationMainDir;
	private File generationTestDir;

	public GenerationContext() {
		this(ProjectFinder.findProjectDir());
	}
	
	public GenerationContext(File rootDir) {
		srcMainDir = new File(rootDir, "src/main/java");
		srcTestDir = new File(rootDir, "src/test/java");
		generationMainDir = new File(rootDir, "src/generated/main/java");
		generationTestDir = new File(rootDir, "src/generated/test/java");
	}

	public File getSrcMainDir() {
		return srcMainDir;
	}

	public void setSrcMainDir(File srcMainDir) {
		this.srcMainDir = srcMainDir;
	}

	public File getSrcTestDir() {
		return srcTestDir;
	}

	public void setSrcTestDir(File srcTestDir) {
		this.srcTestDir = srcTestDir;
	}

	public File getGenerationMainDir() {
		return generationMainDir;
	}

	public void setGenerationMainDir(File generationMainDir) {
		this.generationMainDir = generationMainDir;
	}

	public File getGenerationTestDir() {
		return generationTestDir;
	}

	public void setGenerationTestDir(File generationTestDir) {
		this.generationTestDir = generationTestDir;
	}
}
