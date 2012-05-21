package com.bertvanbrakel.codemucker.pattern;

import static com.google.common.base.Preconditions.checkNotNull;

import com.bertvanbrakel.codemucker.ast.JType;

/**
 * Converts types in a class to short names and adds an import.
 */
public class ImportCleanerPattern {

	private JType target;

	public void apply(){
		checkNotNull(target,"expect target");
		
	}

	public ImportCleanerPattern setTarget(JType target) {
		this.target = target;
		return this;
	}
}
