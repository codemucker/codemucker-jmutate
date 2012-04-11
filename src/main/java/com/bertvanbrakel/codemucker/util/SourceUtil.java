package com.bertvanbrakel.codemucker.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.ast.AssertingAstMatcher;
import com.bertvanbrakel.codemucker.ast.AstCreator;
import com.bertvanbrakel.codemucker.ast.DefaultAstCreator;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.bean.BeanGenerationException;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.ClassPathRoot;
import com.bertvanbrakel.test.util.ProjectFinder;

public class SourceUtil {

	private static AtomicLong uniqueIdCounter = new AtomicLong();

	public static JSourceFile writeNewJavaFile(SrcWriter writer) {
		return writeJavaSrc(writer, findRootDir(), newJavaFQDN());
	}

	public static JSourceFile writeJavaSrc(SrcWriter writer, String fqClassName) {
		return writeJavaSrc(writer, findRootDir(), fqClassName);
	}
	
	public static JSourceFile writeJavaSrc(SrcWriter writer, File rootDir, String fqClassName) {
		String relPath = fqClassName.replace('.', '/') + ".java";
		ClassPathResource resource = writeResource(writer, rootDir, relPath);
		JSourceFile srcFile = new JSourceFile(resource, new DefaultAstCreator());
		return srcFile;
	}

	public static ClassPathResource writeResource(SrcWriter writer) {
		return writeResource(writer,newResourceName());
	}
	
	public static ClassPathResource writeResource(SrcWriter writer, String relPath) {
		return writeResource(writer, findRootDir(), relPath);
	}

	private static File findRootDir() {
	    return new File(ProjectFinder.findTargetDir(), "junit-test-generate");
    }
	
	public static ClassPathResource writeResource(SrcWriter writer, File rootDir, String relPath) {
		ClassPathRoot root = new ClassPathRoot(rootDir);
		File resourceFile = new File(rootDir.getAbsolutePath(), relPath);
		ClassPathResource resource = new ClassPathResource(root, resourceFile, relPath, false);
		writeResource(writer, resource);
		return resource;
	}

	public static ClassPathResource writeResource(SrcWriter writer, ClassPathResource resource) {
		writeFile(writer, resource.getFile());
		return resource;
	}
	
	public static void writeFile(SrcWriter writer, File destFile) {
		FileOutputStream fos = null;
		try {
			if( !destFile.exists()){
				destFile.getParentFile().mkdirs();
				destFile.createNewFile();
			}
			fos = new FileOutputStream(destFile);
			IOUtils.write(writer.getSource(), fos);
		} catch (FileNotFoundException e) {
	        throw new BeanGenerationException("Can't find file " + destFile==null?null:destFile.getAbsolutePath(),e);
        } catch (IOException e) {
	        throw new BeanGenerationException("Error writing to file " + destFile.getAbsolutePath(),e);
        } finally {
			IOUtils.closeQuietly(fos);
		}
	}
	
	public static void assertSourceFileAstsMatch(ClassPathResource expected, ClassPathResource actual) {
		assertSourceFileAstsMatch(expected.getFile(), actual.getFile());
	}

	public static void assertSourceFileAstsMatch(File expected, File actual) {
		assertTrue("Sources AST's don't match", sourceFileAstsMatch(expected, actual));
	}

	public static boolean sourceFileAstsMatch(File expectSrc, File actualSrc) {
		CompilationUnit expectCu = getAstFromFileWithNoErrors(expectSrc);
		CompilationUnit actualCu = getAstFromFileWithNoErrors(actualSrc);

		boolean matchDocTags = false;
		AssertingAstMatcher matcher = new AssertingAstMatcher(matchDocTags);
		return expectCu.subtreeMatch(matcher, actualCu);
	}

	public static JSourceFile getJavaSourceFrom(ClassPathResource resource) {
		return new JSourceFile(resource, newDefaultAstCreator());
	}

	public static AstCreator newDefaultAstCreator(){
		return new DefaultAstCreator();
	}
	
	public static CompilationUnit getAstFromFileWithNoErrors(File srcFile) {
		CompilationUnit cu = getAstFromFile(srcFile);
		
		IProblem[] problems = cu.getProblems();
		assertEquals("Expected no problems for file " + srcFile.getAbsolutePath(), Arrays.asList(new IProblem[]{}), Arrays.asList(problems));

		return cu;
	}
	
	public static CompilationUnit getAstFromFile(File srcFile) {
		String src = readSource(srcFile);
		return getAstFromSrc(src);
	}

	public static String readSource(File srcFile) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(srcFile);
			return IOUtils.toString(fis);
		} catch (IOException e){
			throw new BeanGenerationException("error reading source file " + srcFile.getAbsolutePath(), e);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
	
	public static TypeDeclaration getAstFromClassBody(String src) {
		TypeDeclaration result;
		try {
			ASTParser parser = newParser();
			parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
			parser.setSource(src.toCharArray());

			result = (TypeDeclaration) parser.createAST(null);
		} catch (Exception e) {
			throw new BeanGenerationException("error parsing source", e);
		}
		assertNotNull(result);
		return result;
	}
	
	public static CompilationUnit getAstFromSrc(String src) {
		CompilationUnit result;
		try {
			ASTParser parser = newParser();
			parser.setSource(src.toCharArray());

			result = (CompilationUnit) parser.createAST(null);
		} catch (Exception e) {
			throw new BeanGenerationException("error parsing source", e);
		}
		assertNotNull(result);
		return result;
	}

	public static ASTParser newParser(){
		ASTParser parser = ASTParser.newParser(AST.JLS3);

		// In order to parse 1.5 code, some compiler options need to be set
		// to 1.5
		@SuppressWarnings("rawtypes")
        Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
		parser.setCompilerOptions(options);
		
		return parser;
	}
	
	private static String newJavaFQDN(){
		return "com.bertvanbrakel.codemucker.randomjunit.Java" + uniqueIdCounter.incrementAndGet();
	}
	
	private static String newResourceName(){
		return "com/bertvanbrakel/codemucker/randomjunit/Resource" + uniqueIdCounter.incrementAndGet() + ".text";
	}
}
