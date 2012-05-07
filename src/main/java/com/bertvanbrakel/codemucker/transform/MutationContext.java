package com.bertvanbrakel.codemucker.transform;


public interface MutationContext {
	//JAstParser getParser();
	SourceTemplate newSourceTemplate();
	//StringTemplate newStringTemplate();
	<T> T create(Class<T> type);
	<T> T fill(T instance);
	
	//below should all now be provided via 'create' above
	//PlacementStrategies getStrategies();
	
	
	//could cache some of this stuff, and track changes?
//	JTypeMutator getMutator(AbstractTypeDeclaration type);
//	JTypeMutator getMutator(JType type);
//	JFieldM getMutator(FieldDeclaration field);
//	JTypeMutator getMutator(JField field);
//	JSourceFileMutator getMutator(CompilationUnit cu);
//	JSourceFileMutator getMutator(JSourceFile source);
}
