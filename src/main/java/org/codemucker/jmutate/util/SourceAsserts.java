package org.codemucker.jmutate.util;

import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;

import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.ast.AstNodeProvider;
import org.codemucker.jmutate.ast.JAstFlattener;
import org.codemucker.jmutate.ast.JAstMatcher;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class SourceAsserts {

	/**
     * Assert the given resources look the same once parsed into Ast's. Formatting is ignored
     */
    public static void assertAstsMatch(RootResource expected, RootResource actual) {
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
    	ASTMatcher matcher = JAstMatcher.builder().setMatchDocTags(false).build();
    		
    	boolean equals = false;
    	try {
    		equals = expected.subtreeMatch(matcher, actual);
    	} catch( ComparisonFailure e){
    		throw generateComparisonFailure(expected, actual, e);		
    	} catch( AssertionFailedError e){
    		throw generateComparisonFailure(expected, actual, e);	
    	} catch( AssertionError e){
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

	private static ComparisonFailure generateComparisonFailure(ASTNode expected, ASTNode actual, Error e) {
		String expectFromAst = nodeToString(expected);
		String actualFromAst = nodeToString(actual);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		return new ComparisonFailure("Error comparing asts. Dont't match. Exception is " + sw, expectFromAst, actualFromAst);
	}

	private static CompilationUnit getAstFromFileWithNoErrors(RootResource resource) {
    	CompilationUnit cu = JSourceFile.fromResource(resource, JAstParser.newDefaultJParser()).getCompilationUnitNode();
    	return cu;
    }
	
	public static String nodeToString(ASTNode node){
		return JAstFlattener.asString(node);
	}
}
