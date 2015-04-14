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
    public final void generate(ASTNode node, SmartConfig config) {
        if (JType.is(node)) {
            generate(JType.from(node), config);
        } else if (JField.is(node)) {
            generate(JField.from(node), config);
        } else if (JMethod.is(node)) {
            generate(JMethod.from(node), config);
        }
    }

	protected void generate(JType applyToNode, SmartConfig config) {
    }

    protected void generate(JMethod applyToNode, SmartConfig config) {
    }

    protected void generate(JField applyToNode, SmartConfig config) {
    }

}