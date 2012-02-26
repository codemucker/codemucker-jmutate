package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public abstract class AbstractMutation2<T> implements CodeMutation2<T> {

	private final T applyTo;
	private AbstractMutationBuilder<T> builder;
	private MutationHelper helper;

	public AbstractMutation2(JContext context, T applyTo){
		checkNotNull(applyTo, "need to supply a node to apply mutation to");
		this.applyTo = applyTo;
		this.helper = new MutationHelper(context);
		this.builder = new AbstractMutationBuilder<T>(){
			@Override
            public void apply(T applyTo) {
				//do nothing, not using it for this
            }
		};
	}
	
	protected void addToBodyUsingStrategy(JType javaType, ASTNode child, InsertionStrategy strategy) {
	    helper.addToBodyUsingStrategy(javaType, child, strategy);
    }

	protected FieldDeclaration parseField(String fieldSnippet) {
	    return helper.parseField(fieldSnippet);
    }

	protected MethodDeclaration parseConstructor(String ctorSnippet) {
	    return helper.parseConstructor(ctorSnippet);
    }

	protected MethodDeclaration parseMethod(String methodSnippet) {
	    return helper.parseMethod(methodSnippet);
    }

	protected CompilationUnit parseCompilationUnit(String snippetSrc) {
	    return helper.parseCompilationUnit(snippetSrc);
    }
	
	public AbstractMutation2<T> strategyCtor() {
	    builder.strategyCtor();
	    return this;
	}

	public AbstractMutation2<T> strategyFeild() {
	    builder.strategyFeild();
	    return this;
	}

	public AbstractMutation2<T> strategyMethod() {
	    builder.strategyMethod();
	    return this;
	}

	public AbstractMutation2<T> strategyClass() {
	    builder.strategyClass();
	    return this;
	}

	public AbstractMutation2<T> errorOnExisting() {
	    builder.errorOnExisting();
	    return this;
	}

	public AbstractMutation2<T> errorOnExisting(boolean errorOnExiting) {
		builder.errorOnExisting(errorOnExiting);
		return this;
	}

	public AbstractMutation2<T> insertionStrategy(InsertionStrategy insertionStrategy) {
	    builder.insertionStrategy(insertionStrategy);
	    return this;
	}

	public AbstractMutation2<T> snippet(String snippet) {
	    builder.snippet(snippet);
	    return this;
	}

	public AbstractMutation2<T> replace() {
	    builder.replace();
	    return this;
	}

	public AbstractMutation2<T> replace(boolean isReplace) {
	    builder.replace(isReplace);
	    return this;
	}

	public int hashCode() {
	    return builder.hashCode();
    }

	public void setErrorOnExisting(boolean errorOnExiting) {
	    builder.setErrorOnExisting(errorOnExiting);
    }

	public void setInsertionStrategy(InsertionStrategy insertionStrategy) {
	    builder.setInsertionStrategy(insertionStrategy);
    }

	public void setSnippet(String snippet) {
	    builder.setSnippet(snippet);
    }

	public void setReplace(boolean isReplace) {
	    builder.setReplace(isReplace);
    }

	public InsertionStrategy getInsertionStrategy() {
	    return builder.getInsertionStrategy();
    }

	public String getSnippet() {
	    return builder.getSnippet();
    }

	public boolean isErrorOnExisting() {
	    return builder.isErrorOnExisting();
    }

	public boolean isReplace() {
	    return builder.isReplace();
    }

	@Override
    public final void apply() {
		apply(applyTo);
	}

}
