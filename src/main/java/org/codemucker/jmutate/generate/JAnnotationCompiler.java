package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;

import com.google.inject.ImplementedBy;

/**
 * Converts annotations in source form to compiled form
 */
@ImplementedBy(DefaultAnnotationCompiler.class)
public interface JAnnotationCompiler {
    /**
     * Compile the annotation and cache in the given AST node
     * 
     * @param astNode
     * @return the compiled annotation
     */
    Annotation toCompiledAnnotation(org.eclipse.jdt.core.dom.Annotation astNode);

    /**
     * Compile all the given annotations and cache them in the AST node
     * 
     * @param astNodes
     */
    void compileAnnotations(Iterable<org.eclipse.jdt.core.dom.Annotation> astNodes);
}
