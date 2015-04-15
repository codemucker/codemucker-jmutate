package org.codemucker.jmutate.ast;

import java.lang.annotation.Annotation;
import java.util.List;

import org.codemucker.jfind.FindResult;
import org.codemucker.jmatch.Matcher;
import org.eclipse.jdt.core.dom.ASTNode;

public interface Annotations extends AstNodeProvider<ASTNode> {

    public <A extends Annotation> boolean contains(Class<A> annotationClass);

    public boolean contains(Matcher<JAnnotation> matcher);

    public boolean contains(Matcher<JAnnotation> matcher, SearchDepth depth);

    public List<JAnnotation> getAllDirect();

    public List<JAnnotation> getAllIncludeNested();

    public JAnnotation getOrNull(Matcher<JAnnotation> matcher);

    public <A extends Annotation> JAnnotation getOrNull(Class<A> annotationClass);

    public JAnnotation getOrNull(Matcher<JAnnotation> matcher, SearchDepth depth);

    public FindResult<JAnnotation> find(Matcher<JAnnotation> matcher);

    public FindResult<JAnnotation> find(Matcher<JAnnotation> matcher, SearchDepth depth);
}
