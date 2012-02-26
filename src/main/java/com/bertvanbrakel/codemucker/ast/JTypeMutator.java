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
	
	public void addField(String fieldSnippet, Object... args){
		fieldChange(fieldSnippet,args).apply();
	}
	
	public void addMethod(String methodSnippet, Object... args){
		methodChange(methodSnippet,args).apply();
	}
	
	public void addCtor(String ctorSnippet, Object... args){
		ctorChange(ctorSnippet,args).apply();
	}
	
	public AbstractMutation2<AbstractTypeDeclaration> fieldChange(String fieldSnippet,Object...args){
		return Mutations.fieldChange(new DefaultJContext(), javaType.getTypeNode(), String.format(fieldSnippet,args));
	}
	
	public AbstractMutation2<AbstractTypeDeclaration> methodChange(String methodSnippet,Object...args){
		return Mutations.methodChange(new DefaultJContext(), javaType.getTypeNode(), String.format(methodSnippet,args));
	}
	
	public AbstractMutation2<AbstractTypeDeclaration> ctorChange(String ctorSnippet,Object...args){
		return Mutations.constructorChange(new DefaultJContext(), javaType.getTypeNode(), String.format(ctorSnippet,args));
	}
	
	
}