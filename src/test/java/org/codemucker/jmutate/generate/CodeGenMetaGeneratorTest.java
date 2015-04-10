package org.codemucker.jmutate.generate;

import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.JMutateContext;
import org.junit.Assert;
import org.junit.Test;

public class CodeGenMetaGeneratorTest {

	private JMutateContext ctxt = DefaultMutateContext.with().defaults()
			.build();

	@Test
	public void ensureFieldNameCorrrectlyGenerated() {
		CodeGenMetaGenerator info = new CodeGenMetaGenerator(ctxt, MyCodeGenerator.class);

		Assert.assertEquals("codeGenMetaGeneratorTest$MyCodeGenerator",info.getConstantFieldName());
		Assert.assertEquals("org.codemucker.jmutate.generate.CodeGenMeta.codeGenMetaGeneratorTest$MyCodeGenerator",info.getFullConstantFieldPath());
	}

	private static class MyCodeGenerator extends AbstractCodeGenerator<MyAnnotation> {

		@Override
		protected MyAnnotation getAnnotation() {
			return Defaults.class.getAnnotation(MyAnnotation.class);
		}
		
		@MyAnnotation
		private static class Defaults {}
	}
	
	private static @interface MyAnnotation {}
}
