package org.codemucker.jmutate.ast;

import java.lang.annotation.Annotation;
import java.util.List;

import org.codemucker.jfind.FindResult;
import org.codemucker.jmatch.Matcher;

public interface Annotations {

    public <A extends Annotation> boolean contains(Class<A> annotationClass);

    public boolean contains(Matcher<JAnnotation> matcher);

    public boolean contains(Matcher<JAnnotation> matcher, Depth depth);

    public List<JAnnotation> getAllDirect();

    public List<JAnnotation> getAllIncludeNested();

    public JAnnotation get(Matcher<JAnnotation> matcher);

    public <A extends Annotation> JAnnotation get(Class<A> annotationClass);

    public JAnnotation get(Matcher<JAnnotation> matcher, Depth depth);

    public FindResult<JAnnotation> find(Matcher<JAnnotation> matcher);

    public FindResult<JAnnotation> find(Matcher<JAnnotation> matcher, Depth depth);
}
