package org.codemucker.jmutate.generate;

import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.JMutateContext;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Inject;

public class CodeGenMetaGeneratorTest {

	private JMutateContext ctxt = DefaultMutateContext.with().defaults()
			.build();

	@Test
	public void ensureFieldNameCorrrectlyGenerated() {
		CodeGenMetaGenerator info = new CodeGenMetaGenerator(ctxt, MyCodeGenerator.class);

		Assert.assertEquals("codeGenMetaGeneratorTest$MyCodeGenerator",info.getConstantFieldName());
		Assert.assertEquals("org.codemucker.jmutate.generate.CodeGenMeta.codeGenMetaGeneratorTest$MyCodeGenerator",info.getFullConstantFieldPath());
	}

	private static class MyCodeGenerator extends AbstractGenerator<MyAnnotation> {

		@Inject
		public MyCodeGenerator(JMutateContext ctxt) {
			super(ctxt);
		}
	}
	
	private static @interface MyAnnotation {}
}
