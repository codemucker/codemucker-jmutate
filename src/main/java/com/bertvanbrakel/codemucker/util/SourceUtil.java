package com.bertvanbrakel.codemucker.util;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;

import com.bertvanbrakel.codemucker.ast.AstNodeProvider;
import com.bertvanbrakel.codemucker.ast.JAstFlattener;
import com.bertvanbrakel.codemucker.ast.JAstMatcher;
import com.bertvanbrakel.codemucker.ast.JAstParser;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.bean.BeanGenerationException;
import com.bertvanbrakel.codemucker.transform.Template;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.DirectoryRoot;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.util.ProjectFinder;

public class SourceUtil {

	private static AtomicLong uniqueIdCounter = new AtomicLong();

	public static ClassPathResource writeResource(Template writer) {
		return writeResource(writer,newResourceName());
	}
	
	public static ClassPathResource writeResource(Template writer, String relPath) {
		return writeResource(writer, findRootDir(), relPath);
	}

	private static File findRootDir() {
	    return new File(ProjectFinder.findTargetDir(), "junit-test-generate");
    }
	
	public static ClassPathResource writeResource(Template writer, File rootDir, String relPath) {
		Root root = new DirectoryRoot(rootDir);
		ClassPathResource resource = new ClassPathResource(root, relPath);
		writeResource(writer, resource);
		return resource;
	}

	public static ClassPathResource writeResource(Template writer, ClassPathResource resource) {
		OutputStream os = null;
		try {
			os = resource.getOutputStream();
			IOUtils.write(writer.interpolate(), os);
		} catch (FileNotFoundException e) {
	        throw new BeanGenerationException("Can't find resource " + resource,e);
        } catch (IOException e) {
	        throw new BeanGenerationException("Error writing resource " + resource,e);
        } finally {
			IOUtils.closeQuietly(os);
		}
		
		return resource;
	}
	
	/**
	 * Assert the given resources look the same once parsed into Ast's. This ignores whitespace and newline formatting
	 */
	public static void assertAstsMatch(ClassPathResource expected, ClassPathResource actual) {
		CompilationUnit expectCu = getAstFromFileWithNoErrors(expected);
		CompilationUnit actualCu = getAstFromFileWithNoErrors(actual);
		
		assertAstsMatch(expectCu,actualCu);
	}

	
	public static <T extends ASTNode> void assertAstsMatch(AstNodeProvider<T> expected, AstNodeProvider<T> actual) {
		assertAstsMatch(expected.getAstNode(),actual.getAstNode());
	}
	
	/**
	 * Assert the given Nodes look the same.
	 */
	public static void assertAstsMatch(ASTNode expected, ASTNode actual) {
		ASTMatcher matcher = JAstMatcher.newBuilder().setMatchDocTags(false).build();
			
		boolean equals = false;
		try {
			equals = expected.subtreeMatch(matcher, actual);
		} catch( AssertionFailedError e){
			String expectFromAst = nodeToString(expected);
			String actualFromAst = nodeToString(actual);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			
			throw new ComparisonFailure("Error comparing asts. Dont't match. Exception is " + sw, expectFromAst, actualFromAst);		
		}
		if (!equals) {
			String expectFromAst = nodeToString(expected);
			String actualFromAst = nodeToString(actual);
			throw new ComparisonFailure("Ast's don't match", expectFromAst, actualFromAst);
		}
		assertTrue("ast's don't match", equals);
		//assertEquals(expectAst, actualAst);
	}
	
	private static String nodeToString(ASTNode node){
		return JAstFlattener.asString(node);
	}

	public static JSourceFile getJavaSourceFrom(ClassPathResource resource) {
		return JSourceFile.fromResource(resource, JAstParser.newDefaultParser());
	}

	public static CompilationUnit getAstFromFileWithNoErrors(ClassPathResource resource) {
		CompilationUnit cu = JSourceFile.fromResource(resource, JAstParser.newDefaultParser()).getCompilationUnit();
		return cu;
	}

	private static String newResourceName(){
		return "com/bertvanbrakel/codemucker/randomjunit/Resource" + uniqueIdCounter.incrementAndGet() + ".text";
	}
}