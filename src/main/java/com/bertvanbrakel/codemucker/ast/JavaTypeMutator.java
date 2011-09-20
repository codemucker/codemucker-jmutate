package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.bean.BeanGenerationException;
import com.bertvanbrakel.lang.interpolator.Interpolator;

public class JavaTypeMutator {
	
	private final JavaType javaType;

	private final DefaultStrategyProvider strategies = new DefaultStrategyProvider();
	
	public JavaTypeMutator(JavaSourceFile declaringSrcFile, AbstractTypeDeclaration type) {
		this(new JavaType(declaringSrcFile, type));
	}
	
	public JavaTypeMutator(JavaType javaType) {
		checkNotNull("javaType", javaType);
		this.javaType = javaType;
	}

	public JavaType getJavaType() {
    	return javaType;
    }
	
	public void setAccess(Access access){
		javaType.getJavaModifiers().setAccess(access);
	}
	
	public JavaModifiers getJavaModifiers(){
		return javaType.getJavaModifiers();
	}
	
	
	public void addFieldSnippet(String fieldSnippet) {
		String src = wrapSnippetInClassDeclaration(fieldSnippet);

		TypeDeclaration type = parseSnippetAsClass(src);
		FieldDeclaration field = type.getFields()[0];

		addToBodyAfterBefore(field, strategies.getDefaultFieldStrategy());
	}

	public void addConstructorSnippet(String ctorSnippet) {
		String src = wrapSnippetInClassDeclaration(ctorSnippet);

		TypeDeclaration type = parseSnippetAsClass(src);
		MethodDeclaration method = type.getMethods()[0];
		if (method.getReturnType2() != null) {
			throw new BeanGenerationException("Constructors should not have any return type. Constructor was %s",
			        method);
		}
		if (!method.getName().getIdentifier().equals(type.getName().getIdentifier())) {
			throw new BeanGenerationException(
			        "Constructors should have the same name as the type. Expected name '%s' but got '%s'", type
			                .getName().getIdentifier(), method.getName().getIdentifier());
		}

		method.setConstructor(true);

		addToBodyAfterBefore(method, strategies.getCtorStrategy());
	}

	public void addMethodSnippet(String methodDeclaration) {
		String src = wrapSnippetInClassDeclaration(methodDeclaration);

		TypeDeclaration type = parseSnippetAsClass(src);
		MethodDeclaration method = type.getMethods()[0];
		method.setConstructor(false);

		addToBodyAfterBefore(method, strategies.getDefaultMethodStrategy());
	}

	private String wrapSnippetInClassDeclaration(String snippetSrc) {
		String simpleClassName = javaType.getSimpleName();
		String wrappedSrc = "class " + simpleClassName + "{" + snippetSrc + "}";
		return wrappedSrc;
	}

	public void addClassSnippet(String src) {
		TypeDeclaration type = parseSnippetAsClass(src);
		addToBodyAfterBefore(type, strategies.getClassStrategy());
	}

	private void addToBodyAfterBefore(ASTNode child, InsertionStrategy strategy) {
		ASTNode copy = ASTNode.copySubtree(javaType.getAst(), child);
		List<ASTNode> body = javaType.getBodyDeclarations();
		int index = strategy.findIndex(body);
		body.add(index, copy);
	}

	public TypeDeclaration parseSnippetAsClass(String snippetSrc) {
		CompilationUnit cu = parseSnippet(snippetSrc);
		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);

		return type;
	}

	public CompilationUnit parseSnippet(String snippetSrc) {
		// get template variables and interpolate
		//TODO:add defaults vars
		Map<String, Object> vars = new HashMap<String, Object>();
		CharSequence interpolatedSrc = Interpolator.interpolate(snippetSrc, vars);
		// parse it
		//TODO:jealous class, knows too much about the javaType/ASTCreator. Use a generation context?
		CompilationUnit cu = javaType.getDeclaringSourceFile().getAstCreator().parseCompilationUnit(interpolatedSrc);
		return cu;
	}
}