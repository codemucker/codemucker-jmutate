package org.codemucker.jmutate.bean;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.ast.BaseASTVisitor;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jtest.bean.BeanDefinition;
import org.codemucker.jtest.bean.PropertiesExtractor;
import org.codemucker.lang.annotation.NotThreadSafe;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;


/**
 * Generate bean builders from found classes.
 * 
 * Modify src dir? specify src dir? aka src/main/generated...
 * 
 * 
 * fail if builder/factory not found? fail if builder does not cover all bean
 * aspects? fail if builder does not correctly create beans? fail if builder
 * does not use ctor generation correctly?
 * 
 * provide option to run from ide/main to generate missing (main(..))
 * 
 * todo:find not property methods (fooSOmething(args) with non self return type
 * or void), akak all other non property methods)
 */
@NotThreadSafe
@Deprecated
public class BeanBuilderGenerator {

	private static final Logger LOG =  Logger.getLogger(BeanBuilderGenerator.class);
	
	private final GenerationContext generationCtxt;

	public BeanBuilderGenerator() {
		this(new GenerationContext());
	}

	public BeanBuilderGenerator(GenerationContext ctxt) {
		this.generationCtxt = ctxt;
	}

	public void generate(Class<?> beanClass, GeneratorOptions options) {
		BeanDefinition def = new PropertiesExtractor(options).extractBeanDef(beanClass);
		generate(beanClass.getName(), def, options);
	}

		// TODDO:use a proper AST library, Rescriptor etc....
		// for now a simple string builder
	public void generate(String fqBeanClassName, BeanDefinition def, GeneratorOptions options) {
		generate(new BeanWriter(fqBeanClassName), def);
		generate(new BeanBuilderWriter(fqBeanClassName), def);
		generate(new BeanReadInterfaceWriter(fqBeanClassName), def);
		generate(new BeanWriteWriter(fqBeanClassName), def);
		generate(new BeanReadWriter(fqBeanClassName), def);
	}
	
	public void generate(AbstractBeanWriter w, BeanDefinition def){
		w.generate(def);
		writeSource(w);	
	}
	
	private void writeSource(AbstractBeanWriter w){	
		String relPath = w.getSourceFilePath();
		File dest = new File(generationCtxt.getGenerationMainDir(), relPath);
		String src = w.toJavaClassString();
		
		CompilationUnit result;
		try {
			result = JAstParser.newDefaultJParser().parseCompilationUnit(src, null);
		} catch (Exception e) {
			throw new BeanGenerationException("error parsing source", e);
		}
		assertNotNull(result);
		//System.out.println(ToStringBuilder.reflectionToString(result));
		assertNotNull(result.getRoot());
		//System.out.println(rootNode.getClass().getName());
		BaseASTVisitor vis = new BaseASTVisitor() {
//			@Override
//			protected boolean visitNode(ASTNode node) {
//				System.out.println("------visit---------------");
//				System.out.println(node);
//				return true;
//			}

			@Override
            public boolean visit(MethodDeclaration node) {
	            return log("methodDecl", node);
            }

			@Override
            public boolean visit(MethodInvocation node) {
	            return log("methodCall", node);
            }

			@Override
            public boolean visit(MethodRef node) {
	            return log("methodRef", node);
            }
			
			private boolean log(String msg, ASTNode node){
				System.out.println("---------------------" + msg);
				System.out.println(node);
				return true;
			}
		};
	//	result.accept(vis);
//		System.out.println("rootNode=" + rootNode);
//		
		//System.out.println("comments=" + result.getCommentList());
//		
//		IJavaModel javaModel = result.getJavaElement().getJavaModel();
//		assertNotNull(javaModel);
//		
		writeTo(dest, src);	
	}

	private void writeTo(File dest, String src) {
		FileOutputStream fos = null;
		try {
			if( LOG.isDebugEnabled()){
				LOG.debug("writing source to " + dest.getAbsolutePath());
			}
			dest.getParentFile().mkdirs();
			fos = new FileOutputStream(dest);
			IOUtils.write(src, fos);
		} catch (IOException e) {
			throw new BeanGenerationException("Could not write source destintion file %s", e, dest);
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}
}
