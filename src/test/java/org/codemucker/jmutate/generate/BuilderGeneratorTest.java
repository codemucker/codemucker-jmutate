package org.codemucker.jmutate.generate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.TestSourceHelper;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.generate.builder.BuilderGenerator;
import org.codemucker.jpattern.generate.GenerateBuilder;
import org.codemucker.testfirst.Scenario;
import org.junit.Ignore;
import org.junit.Test;

public class BuilderGeneratorTest {

	JMutateContext ctxt = DefaultMutateContext.with().defaults().build();
	
	@Ignore("Implement me")
	@Test
	public void test(){
		//TODO:copy to tmp dir
		
		TestSourceFile source;
		BuilderGenerator generator;
		
		//what we want to make work
		/*
		scenario()
			.given(generator = new BuilderGenerator(ctxt))
			.given(source = TestSourceFile.from(MyBean.class).createCopy(ctxt))
			.when(generator.generate(source.getTypeNode(), MyBean.class.getAnnotation(GenerateBuilder.class)))
			.then(Expect.that(source).)
			;
*/
		//old way of doing it
//		BuilderGenerator generator = new BuilderGenerator(ctxt);
//		JType node = TestSourceHelper.findTypeNodeForClass(MyBean.class);
//		generator.generate(node, MyBean.class.getAnnotation(GenerateBuilder.class));
//		
		//now get modified source and check is what is expected
	}
	
	private Scenario scenario(){
		Scenario s = new Scenario("mytest");
		
		return s;
	}

	@GenerateBuilder
	private static class MyBean {
		private String field1;
	}
	
	public static class TestSourceFile {

		JType node;
		
		public TestSourceFile(JType node) {
			this.node = node;
		}

		public TestSourceFile createCopy(JMutateContext ctxt) {
			RootResource resource = node.getJCompilationUnit().getSource().getResource();
			RootResource resourceCopy = makeCopy(resource,ctxt.getProjectLayout().newTmpSubDir("cloned_node"));
			
			JSourceFile source = ctxt.getOrLoadSource(resourceCopy);
			List<JType> types = source.findTypesMatching(AJType.with().fullName(node.getFullName())).toList();
			if(types.size() == 0){
				throw new JMutateException("couldn't find node named '" + node.getFullName() + "' within source " + resource.getFullPath());
			}
			if(types.size() > 1){
				throw new JMutateException("multiple nodes named '" + node.getFullName() + "' (expected exactly one) within source " + resource.getFullPath());
			}
			
			this.node = types.get(0);
			return this;
		}
		
		private RootResource makeCopy(RootResource source,File targetRoot){
			Root rootCopy = new DirectoryRoot(targetRoot, RootType.UNKNOWN /* TMP??*/, RootContentType.SRC);
			RootResource resourceCopy = rootCopy.getResource(source.getRelPath());
			try {
				copyTo(source, resourceCopy);
			} catch (IOException e) {
				throw new JMutateException("Couldn't make a copy of resource " + source.getFullPath() + " into root " + targetRoot.getAbsolutePath() ,e);
			}
			return resourceCopy;
		}

		private void copyTo(RootResource source,RootResource destination) throws IOException {
			File f = new File(destination.getFullPath());
			if(!f.exists()){
				f.createNewFile();
			}
			OutputStream os = null;
			InputStream is = null;
			try {
				os = destination.getOutputStream();
				is = source.getInputStream();
				IOUtils.copy(is, os);
			} finally {
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(os);
			}
		}

		public JType getTypeNode() {
			return node;
		}


		public static TestSourceFile from(Class<?> findSourceForClass) {
			JType node = TestSourceHelper.findTypeNodeForClass(findSourceForClass);
			return from(node);
		}
		
		public static TestSourceFile from(JType node) {
			
			return new TestSourceFile(node);
		}
		
		
	}

	
}

