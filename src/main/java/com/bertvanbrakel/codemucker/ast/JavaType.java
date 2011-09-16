package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class JavaType {

	private final JavaSourceFile declaringSrcFile;
	private final AbstractTypeDeclaration type;

	public JavaType(JavaSourceFile declaringSrcFile, AbstractTypeDeclaration type) {
		checkNotNull("declaringSrcFile", declaringSrcFile);
		checkNotNull("type", type);
		this.declaringSrcFile = declaringSrcFile;
		this.type = type;
	}

	public AbstractTypeDeclaration getType() {
    	return type;
    }
	
	public JavaSourceFile getDeclaringFile() {
		return declaringSrcFile;
	}

	protected AST getAst() {
		return type.getAST();
	}

	public String getSimpleName(){
		return type.getName().getIdentifier();
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

	@SuppressWarnings("unchecked")
    public List<ASTNode> getBodyDeclarations() {
	    return type.bodyDeclarations();
    }

	public boolean isAnonymousClass() {
	    throw new UnsupportedOperationException("TODO, implements me!");
    }

	public boolean isImplementing(Class<?> require) {
	    throw new UnsupportedOperationException("TODO, implements me!");
    }
}
