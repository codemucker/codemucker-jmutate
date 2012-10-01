package com.bertvanbrakel.codemucker.util;

import static junit.framework.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.codemucker.ast.AstNodeProvider;
import com.bertvanbrakel.codemucker.ast.JAstFlattener;
import com.bertvanbrakel.codemucker.ast.JAstMatcher;
import com.bertvanbrakel.codemucker.ast.JAstParser;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.test.finder.ClassPathResource;

public class SourceAsserts {

	/**
     * Assert the given resources look the same once parsed into Ast's. Formatting is ignored
     */
    public static void assertAstsMatch(ClassPathResource expected, ClassPathResource actual) {
    	CompilationUnit expectCu = getAstFromFileWithNoErrors(expected);
    	CompilationUnit actualCu = getAstFromFileWithNoErrors(actual);
    	
    	assertAstsMatch(expectCu,actualCu);
    }

    public static <T extends ASTNode> void assertRootAstsMatch(AstNodeProvider<T> expected, AstNodeProvider<T> actual) {
		assertRootAstsMatch(expected.getAstNode(),actual.getAstNode());
	}
    
    /**
     * Assert the Nodes provided look the same. Formatting is ignored
     */
	public static <T extends ASTNode> void assertAstsMatch(AstNodeProvider<T> expected, AstNodeProvider<T> actual) {
    	assertAstsMatch(expected.getAstNode(),actual.getAstNode());
    }

	public static void assertRootAstsMatch(ASTNode expected, ASTNode actual) {
		assertAstsMatch(expected.getRoot(),actual.getRoot());
	}
	/**
     * Assert the given Nodes look the same. Formatting is ignored
     */
    public static void assertAstsMatch(ASTNode expected, ASTNode actual) {
    	ASTMatcher matcher = JAstMatcher.newBuilder().setMatchDocTags(false).build();
    		
    	boolean equals = false;
    	try {
    		equals = expected.subtreeMatch(matcher, actual);
    	} catch( ComparisonFailure e){
    		throw generateComparisonFailure(expected, actual, e);		
    	} catch( AssertionFailedError e){
    		throw generateComparisonFailure(expected, actual, e);	
    	}
    	if (!equals) {
    		String expectFromAst = nodeToString(expected);
    		String actualFromAst = nodeToString(actual);
    		throw new ComparisonFailure("Ast's don't match", expectFromAst, actualFromAst);
    	}
    	assertTrue("ast's don't match", equals);
    	//assertEquals(expectAst, actualAst);
    }

	private static ComparisonFailure generateComparisonFailure(ASTNode expected, ASTNode actual, AssertionFailedError e) {
		String expectFromAst = nodeToString(expected);
		String actualFromAst = nodeToString(actual);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		return new ComparisonFailure("Error comparing asts. Dont't match. Exception is " + sw, expectFromAst, actualFromAst);
	}

	private static CompilationUnit getAstFromFileWithNoErrors(ClassPathResource resource) {
    	CompilationUnit cu = JSourceFile.fromResource(resource, JAstParser.newDefaultParser()).getCompilationUnit();
    	return cu;
    }
	
	public static String nodeToString(ASTNode node){
		return JAstFlattener.asString(node);
	}
}
