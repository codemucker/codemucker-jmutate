package org.codemucker.jmutate.ast.matcher;

import junit.framework.AssertionFailedError;

import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.AstNodeProvider;
import org.codemucker.jmutate.util.SourceAsserts;
import org.eclipse.jdt.core.dom.ASTNode;


public class AnAstNode extends Logical {

	public static Matcher<ASTNode> withSameRootAs(final ASTNode expected){
		return new AbstractNotNullMatcher<ASTNode>() {
			@Override
			public boolean matchesSafely(ASTNode found) {
				try {
					SourceAsserts.assertAstsMatch(expected.getRoot(), found.getRoot());
					return true;
				} catch (AssertionFailedError e){
					return false;
				}
			}
		};
	}
	
	public static Matcher<ASTNode> equalTo(final ASTNode expected){
		return new AbstractNotNullMatcher<ASTNode>() {
			@Override
			public boolean matchesSafely(ASTNode found) {
				try {
					SourceAsserts.assertAstsMatch(expected, found);
					return true;
				} catch (AssertionFailedError e){
					return false;
				}
			}
		};
	}

	public static Matcher<AstNodeProvider<ASTNode>> equalTo(final AstNodeProvider<ASTNode> expected){
		return new AbstractNotNullMatcher<AstNodeProvider<ASTNode>>() {
			@Override
			public boolean matchesSafely(AstNodeProvider<ASTNode> found) {
				try {
					SourceAsserts.assertAstsMatch(expected, found);
					return true;
				} catch (AssertionFailedError e){
					return false;
				}
			}
		};
	}
	
}
