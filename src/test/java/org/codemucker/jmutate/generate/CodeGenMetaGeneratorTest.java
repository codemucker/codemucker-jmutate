package org.codemucker.jmutate.generate;

import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jpattern.generate.GenerateBean;
import org.junit.Assert;
import org.junit.Test;

public class CodeGenMetaGeneratorTest {

	private JMutateContext ctxt = DefaultMutateContext.with().defaults()
			.build();

	@Test
	public void ensureFieldNameCorrrectlyGenerated() {
		CodeGenMetaGenerator info = new CodeGenMetaGenerator(ctxt, MyCodeGenerator.class);

		Assert.assertEquals("CODE_GEN_META_GENERATOR_TEST$_MY_CODE_GENERATOR",info.getConstantFieldName());
		Assert.assertEquals("org.codemucker.jmutate.generate.CodeGenMeta.CODE_GEN_META_GENERATOR_TEST$_MY_CODE_GENERATOR",info.getFullConstantFieldPath());
	}

	private static class MyCodeGenerator extends
			AbstractCodeGenerator<GenerateBean> {

	}
}
