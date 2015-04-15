package org.codemucker.jmutate.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.codemucker.jfind.DefaultFindResult;
import org.codemucker.jfind.FindResult;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 * Represents a node which contains annotations
 */
public abstract class AbstractAnnotations implements Annotations {

    private static final Matcher<JAnnotation> MATCH_ANY = Logical.<JAnnotation> any();

    public abstract ASTNode getAstNode();

    protected abstract List<IExtendedModifier> getModifiers();

    @Override
    public <A extends java.lang.annotation.Annotation> boolean contains(Class<A> annotationClass) {
        return contains(AJAnnotation.with().fullName(annotationClass));
    }

    @Override
    public boolean contains(Matcher<JAnnotation> matcher) {
        return getOrNull(matcher) != null;
    }

    @Override
    public boolean contains(Matcher<JAnnotation> matcher, SearchDepth depth) {
        return getOrNull(matcher, depth) != null;
    }

    @Override
    public List<JAnnotation> getAllDirect() {
        return find(MATCH_ANY, SearchDepth.DIRECT).toList();
    }

    @Override
    public List<JAnnotation> getAllIncludeNested() {
        return find(MATCH_ANY, SearchDepth.ANY).toList();
    }

    @Override
    public <A extends java.lang.annotation.Annotation> JAnnotation getOrNull(Class<A> annotationClass) {
        return getOrNull(AJAnnotation.with().fullName(annotationClass));
    }

    @Override
    public JAnnotation getOrNull(Matcher<JAnnotation> matcher) {
        return getOrNull(matcher, SearchDepth.DIRECT);
    }

    @Override
    public JAnnotation getOrNull(final Matcher<JAnnotation> matcher, SearchDepth depth) {
        if (depth.max == SearchDepth.DIRECT.max) {
            for (IExtendedModifier mod : getModifiers()) {
                if (mod instanceof org.eclipse.jdt.core.dom.Annotation) {
                    JAnnotation anon = JAnnotation.from((org.eclipse.jdt.core.dom.Annotation) mod);
                    if (matcher.matches(anon)) {
                        return anon;
                    }
                }
            }
        } else {
            final AtomicReference<JAnnotation> found = new AtomicReference<JAnnotation>();
            final int maxDepth = depth.max;
            ASTVisitor visitor = new BaseASTVisitor() {
                int depth = -1;

                @Override
                public boolean visitNode(ASTNode node) {
                    depth++;
                    if ((maxDepth > -1 && depth > maxDepth) || found.get() != null) {
                        return false;
                    }
                    if (node instanceof ImportDeclaration) {
                        return false;
                    }
                    if (node instanceof org.eclipse.jdt.core.dom.Annotation) {
                        JAnnotation anon = JAnnotation.from((org.eclipse.jdt.core.dom.Annotation) node);
                        if (matcher.matches(anon)) {
                            found.set(anon);
                            return false;// stop after first match
                        }
                    }
                    return true;
                }

                @Override
                public void endVisitNode(ASTNode node) {
                    depth--;
                }
            };
            getAstNode().accept(visitor);
            JAnnotation a = found.get();
            return a;
        }
        return null;
    }

    @Override
    public FindResult<JAnnotation> find(Matcher<JAnnotation> matcher) {
        return find(matcher, SearchDepth.DIRECT);
    }

    @Override
    public FindResult<JAnnotation> find(final Matcher<JAnnotation> matcher, SearchDepth depth) {
        final List<JAnnotation> found = new ArrayList<>();
        if (depth.max == SearchDepth.DIRECT.max) {
            for (IExtendedModifier mod : getModifiers()) {
                if (mod instanceof org.eclipse.jdt.core.dom.Annotation) {
                    JAnnotation anon = JAnnotation.from((org.eclipse.jdt.core.dom.Annotation) mod);
                    if (matcher.matches(anon)) {
                        found.add(anon);
                    }
                }
            }
        } else {
            final int maxDepth = depth.max;
            ASTVisitor visitor = new BaseASTVisitor() {
                int depth = -1;

                @Override
                public boolean visitNode(ASTNode node) {
                    depth++;
                    if (maxDepth > -1 && depth > maxDepth) {
                        return false;
                    }
                    if (node instanceof ImportDeclaration) {
                        return false;
                    }
                    if (node instanceof org.eclipse.jdt.core.dom.Annotation) {
                        JAnnotation anon = JAnnotation.from((org.eclipse.jdt.core.dom.Annotation) node);
                        if (matcher.matches(anon)) {
                            found.add(anon);
                        }
                    }
                    return true;
                }

                @Override
                public void endVisitNode(ASTNode node) {
                    depth--;
                }
            };
            getAstNode().accept(visitor);
        }
        return DefaultFindResult.from(found);
    }

}
