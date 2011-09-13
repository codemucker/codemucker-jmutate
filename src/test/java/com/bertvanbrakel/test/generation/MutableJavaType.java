package com.bertvanbrakel.test.generation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.test.bean.builder.BeanGenerationException;

public class MutableJavaType {
	private final DefaultStrategyProvider strategies = new DefaultStrategyProvider();
	private final MutableJavaSourceFile declaringSrcFile;
	private final AbstractTypeDeclaration type;

	public MutableJavaType(MutableJavaSourceFile declaringSrcFile, AbstractTypeDeclaration type) {
		this.declaringSrcFile = declaringSrcFile;
		this.type = type;
	}

	public MutableJavaSourceFile getDeclaringFile() {
		return declaringSrcFile;
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
		String simpleClassName = this.type.getName().getIdentifier();
		String wrappedSrc = "class " + simpleClassName + "{" + snippetSrc + "}";
		return wrappedSrc;
	}

	public void addClassSnippet(String src) {
		TypeDeclaration type = parseSnippetAsClass(src);
		addToBodyAfterBefore(type, strategies.getClassStrategy());
	}

	@SuppressWarnings("unchecked")
	private void addToBodyAfterBefore(ASTNode child, InsertionStrategy strategy) {
		ASTNode copy = ASTNode.copySubtree(getAst(), child);
		List<ASTNode> body = type.bodyDeclarations();
		int index = strategy.findIndex(body);
		body.add(index, copy);
	}

	private AST getAst() {
		return type.getAST();
	}

	private static Collection<Class<?>> col(Class<?>... types) {
		return Arrays.asList(types);
	}
	
	public TypeDeclaration parseSnippetAsClass(String snippetSrc) {
		return declaringSrcFile.parseSnippetAsClass(snippetSrc);
	}

	public CompilationUnit parseSnippet(String snippetSrc) {
		return declaringSrcFile.parseSnippet(snippetSrc);
	}

	public boolean isStatic() {
		return Modifier.isStatic(getModifiers());
	}

	public boolean isFinal() {
		return Modifier.isFinal(getModifiers());
	}

	public boolean isPublic() {
		return Modifier.isPublic(getModifiers());
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(getModifiers());
	}

	public int getModifiers() {
		// todo:may want to cache this as this is recalculated on each call
		// in which case e also want to know when they change so cache can be
		// cleared
		return type.getModifiers();
	}

	public boolean isEnum() {
		return type instanceof EnumDeclaration;
	}

	public boolean isAnnotation() {
		return type instanceof AnnotationTypeDeclaration;
	}

	public boolean isInnerClass() {
		return isClass() && type.isMemberTypeDeclaration();
	}

	public boolean isTopLevelClass() {
		return type.isPackageMemberTypeDeclaration();
	}

	public boolean isConcreteClass() {
		return isClass() && !asType().isInterface();
	}

	public boolean isInterface() {
		return isClass() && asType().isInterface();
	}

	// public boolean isAnonymous(){
	// return isClass() && asType().;
	// }
	//
	public boolean isClass() {
		return type instanceof TypeDeclaration;
	}

	public TypeDeclaration asType() {
		return (TypeDeclaration) type;
	}

	public EnumDeclaration asEnum() {
		return (EnumDeclaration) type;
	}

	public AnnotationTypeDeclaration asAnnotation() {
		return (AnnotationTypeDeclaration) type;
	}

	public <A extends Annotation> boolean hasAnnotation(Class<A> anon) {
		return getAnnotation(anon) != null;
	}

	public <A extends Annotation> A getAnnotation(Class<A> anon) {
		return null;
	}
}