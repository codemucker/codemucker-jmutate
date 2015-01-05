package org.codemucker.jmutate.generate;

import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jpattern.generate.GenerateBean;
import org.junit.Assert;
import org.junit.Test;

public class CodeGenInfoTest {

	private JMutateContext ctxt = DefaultMutateContext.with().defaults()
			.build();

	@Test
	public void ensureFieldNameCorrrectlyGenerated() {
		CodeGenMetaGenerator info = new CodeGenMetaGenerator(ctxt, MyCodeGenerator.class);

		Assert.assertEquals("GENERATE_CODEGENINFOTEST$MYCODEGENERATOR",info.getConstantFieldName());
		Assert.assertEquals("org.codemucker.jmutate.generate.CodeGenMeta.GENERATE_CODEGENINFOTEST$MYCODEGENERATOR",info.getFullConstantFieldPath());
	}

	private static class MyCodeGenerator extends
			AbstractCodeGenerator<GenerateBean> {

	}
}
