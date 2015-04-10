package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.eclipse.jdt.core.dom.ASTNode;

public abstract class AbstractCodeGenerator<T extends Annotation> implements CodeGenerator<T> {

	@Override
	public Configuration getDefaultConfig() {
		return new AnnotationConfiguration(getAnnotation());
	}
	
	protected abstract T getAnnotation();
	
	
    @Override
	public void beforeRun() {
	}

	@Override
	public void afterRun() {
	}

	@Override
    public final void generate(ASTNode node, Configuration config) {
        if (JType.is(node)) {
            generate(JType.from(node), config);
        } else if (JField.is(node)) {
            generate(JField.from(node), config);
        } else if (JMethod.is(node)) {
            generate(JMethod.from(node), config);
        }
    }

    protected void generate(JType applyToNode, Configuration config) {
    }

    protected void generate(JMethod applyToNode, Configuration config) {
    }

    protected void generate(JField applyToNode, Configuration config) {
    }

}