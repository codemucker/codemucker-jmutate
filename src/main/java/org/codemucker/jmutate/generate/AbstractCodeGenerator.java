package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;

import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.eclipse.jdt.core.dom.ASTNode;

public abstract class AbstractCodeGenerator<T extends Annotation> implements CodeGenerator<T> {

	@Override
	public void beforeRun() {
	}

	@Override
	public void afterRun() {
	}

	@Override
	public final void generate(ASTNode declaredInNode, SmartConfig config) {
		if (JType.is(declaredInNode)) {
			generate(JType.from(declaredInNode), config);
		} else if (JField.is(declaredInNode)) {
			generate(JField.from(declaredInNode), config);
		} else if (JMethod.is(declaredInNode)) {
			generate(JMethod.from(declaredInNode), config);
		}
	}

	protected void generate(JType declaredInNode, SmartConfig config) {}

	protected void generate(JMethod declaredInNode, SmartConfig config) {}

	protected void generate(JField declaredInNode, SmartConfig config) {}

}