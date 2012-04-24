package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.FindResultIterableBacked;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JFieldMatchers;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.util.JavaNameUtil;
import com.bertvanbrakel.test.finder.matcher.Matcher;

/**
 * A convenience wrapper around an Ast java type
 */
public class JType implements JAnnotatable, AstNodeProvider<AbstractTypeDeclaration> {

	private final AbstractTypeDeclaration typeNode;

	public static final Matcher<JType> MATCH_ALL_TYPES = new Matcher<JType>() {
		@Override
		public boolean matches(JType found) {
			return true;
		}
	};
	
	public static final Matcher<JMethod> MATCH_ALL_METHODS = new Matcher<JMethod>() {
		@Override
		public boolean matches(JMethod found) {
			return true;
		}
	};
	
	public JType(AbstractTypeDeclaration type) {
		checkNotNull("type", type);
		this.typeNode = type;
	}
	
	@Override
	public AbstractTypeDeclaration getAstNode(){
		return typeNode;
	}
	
	public JType getTypeWithName(String simpleName){
		if (!isClass()) {
			throw new CodemuckerException("Type '%s' is not a class so can't search for type named '%s'",getSimpleName(), simpleName);
		}
		TypeDeclaration[] types = asTypeDecl().getTypes();
		for (AbstractTypeDeclaration type : types) {
			if (simpleName.equals(type.getName().toString())) {
				return new JType(type);
			}
		}
		Collection<String> names = extractTypeNames(types);
		throw new CodemuckerException("Can't find type named %s in %s. Found %s", simpleName, this, Arrays.toString(names.toArray()));
	}

	private static Collection<String> extractTypeNames(TypeDeclaration[] types){
		Collection<String> names = newArrayList();
		for( AbstractTypeDeclaration type:types){
			names.add(type.getName().toString());
		}
		return names;
	}
	
	public FindResult<JType> findChildTypesMatching(Matcher<JType> matcher){
		List<JType> found = newArrayList();
		findChildTypesMatching(this, matcher, found);
		return FindResultIterableBacked.from(found);
	}
	
	/* package */ void findChildTypesMatching(Matcher<JType> matcher, List<JType> found){
		findChildTypesMatching(this, matcher, found);
	}
	
	public List<JType> findAllChildTypes() {
		List<JType> found = new ArrayList<JType>();
		findChildTypesMatching(this, MATCH_ALL_TYPES, found);
		return found;
	}
	
	private void findChildTypesMatching(JType type, Matcher<JType> matcher, List<JType> found) {
		if (type.isClass()) {
			for (TypeDeclaration child : type.asTypeDecl().getTypes()) {
				JType childJavaType = new JType(child);
				if (matcher.matches(childJavaType)) {
					found.add(childJavaType);
				}
				findChildTypesMatching(childJavaType, matcher, found);
			}
		}
	}
	
	public JTypeMutator asMutator(MutationContext ctxt){
		return new JTypeMutator(ctxt, this);
	}

	public FindResult<JField> findAllFields(){
		return findFieldsMatching(JFieldMatchers.any());
	}
	public FindResult<JField> findFieldsMatching(Matcher<JField> matcher) {
		List<JField> found = newArrayList();
		findFieldsMatching(matcher, found);
		return FindResultIterableBacked.from(found);
	}
	
	private void findFieldsMatching(final Matcher<JField> matcher, final List<JField> found) {
		int maxDepth = 0;
		final JType parent = this;
		BaseASTVisitor visitor = new IgnoreableChildTypesVisitor(maxDepth) {
			@Override
			public boolean visit(FieldDeclaration node) {
				JField field = new JField(node);
				if (matcher.matches(field)) {
					found.add(field);
				}
				return super.visit(node);
			}
		};
		this.typeNode.accept(visitor);
	}
	
	public FindResult<JMethod> findMethodsMatching(Matcher<JMethod> matcher) {
		List<JMethod> found = newArrayList();
		findMethodsMatching(matcher, found);
		return FindResultIterableBacked.from(found);
	}

	public FindResult<JMethod> findAllJMethods() {
		List<JMethod> found = newArrayList();
		findMethodsMatching(MATCH_ALL_METHODS, found);
		return FindResultIterableBacked.from(found);
	}
	
	private void findMethodsMatching(final Matcher<JMethod> matcher, final List<JMethod> found) {
		int maxDepth = 0;
		final JType parent = this;
		BaseASTVisitor visitor = new IgnoreableChildTypesVisitor(maxDepth) {
			@Override
			public boolean visit(MethodDeclaration node) {
				JMethod javaMethod = new JMethod(node);
				if (matcher.matches(javaMethod)) {
					found.add(javaMethod);
				}
				return super.visit(node);
			}
		};
		this.typeNode.accept(visitor);
	}
	
	public boolean hasMethodsMatching(final Matcher<JMethod> matcher) {
		int maxDepth = 0;
		final JType parent = this;
		final AtomicBoolean foundMethod = new AtomicBoolean();
		
		BaseASTVisitor visitor = new IgnoreableChildTypesVisitor(maxDepth) {
			@Override
			public boolean visit(MethodDeclaration node) {
				JMethod javaMethod = new JMethod(node);
				if (matcher.matches(javaMethod)) {
					foundMethod.set(true);
					return false;
				}
				return super.visit(node);
			}

			@Override
            protected boolean visitNode(ASTNode node) {
				//exit as soon as we've found a matching method
	            return !foundMethod.get();
            }
			
		};
		typeNode.accept(visitor);
		return foundMethod.get();
	}

	public AST getAst() {
		return typeNode.getAST();
	}

	public CompilationUnit getCompilationUnit(){
		ASTNode parent = typeNode;
		while( parent != null){
			parent = parent.getParent();
			if( parent instanceof CompilationUnit){
				return (CompilationUnit)parent;
			}
		}
		throw new CodemuckerException("Couldn't find compilation unit. Unexpected");
	}

	public String getFullName(){
		return JavaNameUtil.getQualifiedNameFor(typeNode);
	}
	
	public String getSimpleName(){
		return typeNode.getName().getIdentifier();
	}

	public String getPackageName(){
		return JavaNameUtil.getPackageFor(typeNode);
	}
	
	public boolean isAccess(JAccess access) {
		return getModifiers().asAccess().equals(access);
	}
	
	@SuppressWarnings("unchecked")
    public JModifiers getModifiers(){
		return new JModifiers(typeNode.getAST(),typeNode.modifiers());
	}
	
	public boolean isEnum() {
		return typeNode instanceof EnumDeclaration;
	}

	public boolean isAnnotation() {
		return typeNode instanceof AnnotationTypeDeclaration;
	}

	public boolean isInnerClass() {
		return isClass() && typeNode.isMemberTypeDeclaration();
	}

	public boolean isTopLevelClass() {
		return typeNode.isPackageMemberTypeDeclaration();
	}

	public boolean isConcreteClass() {
		return isClass() && !asTypeDecl().isInterface();
	}

	public boolean isInterface() {
		return isClass() && asTypeDecl().isInterface();
	}

	public boolean isClass() {
		return typeNode instanceof TypeDeclaration;
	}

	public TypeDeclaration asTypeDecl() {
		return (TypeDeclaration) typeNode;
	}

	public EnumDeclaration asEnumDecl() {
		return (EnumDeclaration) typeNode;
	}

	public AnnotationTypeDeclaration asAnnotationDecl() {
		return (AnnotationTypeDeclaration) typeNode;
	}

	@Override
	public <A extends Annotation> boolean hasAnnotationOfType(Class<A> annotationClass) {
		return hasAnnotationOfType(annotationClass, false);
	}
	
	public <A extends Annotation> boolean hasAnnotationOfType(Class<A> annotationClass,boolean includeChildClassesInLookup) {
		return getAnnotationOfType(annotationClass,includeChildClassesInLookup) != null;
	}

	@Override
	public <A extends Annotation> JAnnotation getAnnotationOfType(Class<A> annotationClass) {
		return getAnnotationOfType(annotationClass,false);
	}
	
	public <A extends Annotation> JAnnotation getAnnotationOfType(Class<A> annotationClass, boolean includeChildClassesInLookup) {
		return JAnnotation.getAnnotationOfType(typeNode, includeChildClassesInLookup?JAnnotation.ANY_DEPTH:JAnnotation.DIRECT_DEPTH, annotationClass);
	}
	
	@Override
	public Collection<org.eclipse.jdt.core.dom.Annotation> getAnnotations(){
		//all
		return getAnnotations(true);
	}
	
	/**
	 * Returns all the annotations attached to this class. This does not include annotation declarations, but rather use.
	 * @return
	 */
	public Collection<org.eclipse.jdt.core.dom.Annotation> getAnnotations(final boolean includeChildClassesInLookup){
		final int maxDepth = includeChildClassesInLookup?-1:0;
		return JAnnotation.findAnnotations(typeNode, maxDepth);
	}

	@SuppressWarnings("unchecked")
    public List<ASTNode> getBodyDeclarations() {
	    return typeNode.bodyDeclarations();
    }

	public boolean isAnonymousClass() {
	    throw new UnsupportedOperationException("TODO, implements me!");
    }

	public boolean isImplementing(Class<?> require) {
	    throw new UnsupportedOperationException("TODO, implements me!");
    }
	
}
