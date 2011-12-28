package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;

public class JTypeMutator {
	
	private final JType javaType;
	
	public JTypeMutator(AbstractTypeDeclaration type) {
		this(new JType(type));
	}
	
	public JTypeMutator(JType javaType) {
		checkNotNull("javaType", javaType);
		this.javaType = javaType;
	}

	public JType getJavaType() {
    	return javaType;
    }
	
	public void setAccess(JAccess access){
		javaType.getJavaModifiers().setAccess(access);
	}
	
	public JModifiers getJavaModifiers(){
		return javaType.getJavaModifiers();
	}
	
	public ImportDeclaration newImport(String fqn){
		AST ast = javaType.getAst();
		Name name = ast.newName(fqn);
		ImportDeclaration imprt = ast.newImportDeclaration();
		imprt.setName(name);
		return imprt;
	}
	
	public void addField(String fieldSnippet){
		Mutations.fieldChange(new DefaultJContext(), fieldSnippet).apply(javaType.getTypeNode());
	}
	
	public void addMethod(String methodSnippet){
		Mutations.methodChange(new DefaultJContext(), methodSnippet).apply(javaType.getTypeNode());
	}
	
	public void addCtor(String ctorSnippet){
		Mutations.constructorChange(new DefaultJContext(), ctorSnippet).apply(javaType.getTypeNode());
	}
	
}