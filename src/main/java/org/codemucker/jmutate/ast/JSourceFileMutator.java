package org.codemucker.jmutate.ast;

import static com.google.common.collect.Lists.newArrayList;
import static org.codemucker.lang.Check.checkNotNull;

import java.util.List;

import org.codemucker.jmutate.transform.MutateContext;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class JSourceFileMutator {
	private final JSourceFile source;
	private final MutateContext context;

	public JSourceFileMutator(MutateContext context, JSourceFile source) {;
		this.context = checkNotNull("context", context);
		this.source = checkNotNull("source", source);
	}
	
	public JSourceFile getJSource() {
		return source;
	}

	public CompilationUnit getCompilationUnit() {
		return source.getCompilationUnitNode();
	}

	/**
	 * Return the type declared in this file with the same name as the name of the file.
	 */
	public JTypeMutator getMainTypeAsMutable() {
		return new JTypeMutator(context, source.getMainType());
	}
	
	/**
	 * Return all the top level java types declared in this file
	 */
	public Iterable<JTypeMutator> getTypesAsMutable() {
		List<JTypeMutator> mutables = newArrayList();
		List<AbstractTypeDeclaration> types = getCompilationUnit().types();
		for (AbstractTypeDeclaration type : types) {
			mutables.add(new JTypeMutator(context, type));
		}
		return mutables;
	}
}