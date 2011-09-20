package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.ast.finder.JavaTypeMatcher;

/**
 * A convenience wrapper around an Ast java type
 */
public class JavaType {

	private final JavaSourceFile declaringSourceFile;
	private final AbstractTypeDeclaration type;

	private static final JavaTypeMatcher MATCH_ALL = new JavaTypeMatcher() {
		@Override
		public boolean matchType(JavaType found) {
			return true;
		}
	};
	public JavaType(JavaSourceFile declaringSrcFile, AbstractTypeDeclaration type) {
		checkNotNull("declaringSrcFile", declaringSrcFile);
		checkNotNull("type", type);
		this.declaringSourceFile = declaringSrcFile;
		this.type = type;
	}
	
	public JavaType getTypeWithName(String simpleName){
		if (!isClass()) {
			throw new CodemuckerException("Type '%s' is not a class so can't search for type named '%s'",getSimpleName(), simpleName);
		}
		TypeDeclaration[] types = asType().getTypes();
		for (AbstractTypeDeclaration type : types) {
			if (simpleName.equals(type.getName().toString())) {
				return new JavaType(declaringSourceFile, type);
			}
		}
		Collection<String> names = extractNames(types);
		throw new CodemuckerException("Can't find type named %s in %s. Found %s", simpleName, declaringSourceFile.getLocation().getRelativePath(), Arrays.toString(names.toArray()));
	}

	private static Collection<String> extractNames(TypeDeclaration[] types){
		Collection<String> names = new ArrayList<String>();
		for( AbstractTypeDeclaration type:types){
			names.add(type.getName().toString());
		}
		return names;
	}
	public List<JavaType> findChildTypesMatching(JavaTypeMatcher matcher){
		List<JavaType> found = new ArrayList<JavaType>();
		findChildTypesMatching(this, matcher, found);
		return found;
	}
	
	/* package */ void findChildTypesMatching(JavaTypeMatcher matcher, List<JavaType> found){
		findChildTypesMatching(this, matcher, found);
	}
	
	public List<JavaType> findAllChildTypes() {
		List<JavaType> found = new ArrayList<JavaType>();
		findChildTypesMatching(this, MATCH_ALL, found);
		return found;
	}
	
	private void findChildTypesMatching(JavaType type, JavaTypeMatcher matcher, List<JavaType> found) {
		if (type.isClass()) {
			for (TypeDeclaration child : type.asType().getTypes()) {
				JavaType childJavaType = new JavaType(declaringSourceFile, child);
				if (matcher.matchType(childJavaType)) {
					found.add(childJavaType);
				}
				findChildTypesMatching(childJavaType, matcher, found);
			}
		}
	}
	
	public JavaTypeMutator asMutator(){
		return new JavaTypeMutator(this);
	}
	
	public AbstractTypeDeclaration getType() {
    	return type;
    }
	
	public JavaSourceFile getDeclaringSourceFile() {
		return declaringSourceFile;
	}

	protected AST getAst() {
		return type.getAST();
	}

	public String getSimpleName(){
		return type.getName().getIdentifier();
	}

	@SuppressWarnings("unchecked")
    public JavaModifiers getJavaModifiers(){
		return new JavaModifiers(type.getAST(),type.modifiers());
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
