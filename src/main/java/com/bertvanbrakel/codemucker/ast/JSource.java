package com.bertvanbrakel.codemucker.ast;

import com.bertvanbrakel.codemucker.ast.finder.ClasspathResource;

/**
 * Represents the source location of a code snippet. THis could be a file, a resource, a snippet
 */
public interface JSource {

	ClasspathResource getLocation();

	AstCreator getAstCreator();

}
