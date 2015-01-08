package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.TestSourceHelper;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jpattern.generate.Access;
import org.junit.Assert;
import org.junit.Test;

public class DefaultAnnotationCompilerTest {

	private JMutateContext ctxt = DefaultMutateContext.with()
			.defaults()
			.build();

	@Test
	public void canCompileAnnotation() {

		DefaultAnnotationCompiler compiler = new DefaultAnnotationCompiler(ctxt);
		
		JSourceFile source = TestSourceHelper.findSourceForClass(MyTestClass.class);
		
		JType myClass = source.getMainType().findTypesMatching(AJType.with().annotation(MyTestAnnotation.class)).getFirstOrNull();
	
		JAnnotation myAnon = myClass.getAnnotations().get(MyTestAnnotation.class);
		Annotation anon = compiler.toCompiledAnnotation(myAnon.getAstNode());

		//need to use reflection as different classloaders are used (mainly so we can drop the classloader when generation complete)
		//so we can't cast to MyTestAnnotation
		Assert.assertEquals(getAttribute(anon,"someStringField"), "some value");
		Assert.assertEquals(getAttribute(anon,"someClassField"), Map.class);
		Assert.assertEquals(getAttribute(anon,"someEnumField.toString"), Access.PROTECTED.name());
		
	}
	
	private Object getAttribute(Object instance,String path){
		Object val = instance;
		for(String name:path.split("\\.")){
			Method m;
			try {
				m = val.getClass().getMethod(name, new Class[]{});
				val = m.invoke(val, new Object[]{});
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionFailedError("No method named '" + name + "'. Exception=" + e.getMessage());
			}
		}
		return val;
	}
	
	
	@MyTestAnnotation(someClassField=Map.class, someStringField="some value",someEnumField=Access.PROTECTED)
	private static class MyTestClass {
	
	}


}
