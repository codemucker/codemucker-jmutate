package com.bertvanbrakel.codemucker.transform;

import com.bertvanbrakel.codemucker.ast.JAstParser;

public interface MutationContext {
	JAstParser getParser();
	PlacementStrategies getStrategies();
	SourceTemplate newSourceTemplate();
	StringTemplate newStringTemplate();
	
	//could cache some of this stuff, and track changes?
//	JTypeMutator getMutator(AbstractTypeDeclaration type);
//	JTypeMutator getMutator(JType type);
//	JFieldM getMutator(FieldDeclaration field);
//	JTypeMutator getMutator(JField field);
//	JSourceFileMutator getMutator(CompilationUnit cu);
//	JSourceFileMutator getMutator(JSourceFile source);
}
