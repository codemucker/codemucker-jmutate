package com.bertvanbrakel.codemucker.ast.matcher;

import junit.framework.AssertionFailedError;

import org.eclipse.jdt.core.dom.ASTNode;

import com.bertvanbrakel.codemucker.ast.AstNodeProvider;
import com.bertvanbrakel.codemucker.util.SourceAsserts;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;

public class ASTNodeMatchers extends LogicalMatchers {

	public static Matcher<ASTNode> equalTo(final ASTNode expected){
		return new Matcher<ASTNode>() {
			@Override
			public boolean matches(ASTNode found) {
				try {
					SourceAsserts.assertAstsMatch(expected, found);
					return true;
				} catch (AssertionFailedError e){
					return false;
				}
			}
		};
	}
	
	public static Matcher<ASTNode> rootsEqualTo(final ASTNode expected){
		return new Matcher<ASTNode>() {
			@Override
			public boolean matches(ASTNode found) {
				try {
					SourceAsserts.assertAstsMatch(expected.getRoot(), found.getRoot());
					return true;
				} catch (AssertionFailedError e){
					return false;
				}
			}
		};
	}

	public static Matcher<AstNodeProvider<ASTNode>> equalTo(final AstNodeProvider<ASTNode> expected){
		return new Matcher<AstNodeProvider<ASTNode>>() {
			@Override
			public boolean matches(AstNodeProvider<ASTNode> found) {
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
