package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.codemucker.transform.MutationContext;

public class JSourceFileMutator {
	private final JSourceFile source;
	private final MutationContext context;

	public JSourceFileMutator(MutationContext context, JSourceFile source) {;
		this.context = checkNotNull("context", context);
		this.source = checkNotNull("source", source);
	}
	
	public JSourceFile getJSource() {
		return source;
	}

	public CompilationUnit getCompilationUnit() {
		return source.getCompilationUnit();
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