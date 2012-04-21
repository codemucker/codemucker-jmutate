package com.bertvanbrakel.codemucker.ast;

import com.bertvanbrakel.test.finder.ClassPathResource;

/**
 * Represents the source location of a code snippet. THis could be a file, a resource, a snippet
 */
public interface JSource {

	ClassPathResource getLocation();

}
