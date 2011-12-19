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
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.ast.finder.matcher.JMethodMatcher;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JTypeMatcher;
import com.bertvanbrakel.codemucker.ast.finder.matcher.Matcher;
import com.bertvanbrakel.codemucker.util.JavaNameUtil;

/**
 * A convenience wrapper around an Ast java type
 */
public class JType {

	private final JSource source;
	private final AbstractTypeDeclaration typeNode;

	public static final JTypeMatcher MATCH_ALL_TYPES = new JTypeMatcher() {
		@Override
		public boolean matches(JType found) {
			return true;
		}
	};
	
	public static final JMethodMatcher MATCH_ALL_METHODS = new JMethodMatcher() {
		@Override
		public boolean matches(JMethod found) {
			return true;
		}
	};
	
	public JType(JSource source, AbstractTypeDeclaration type) {
		checkNotNull("source", source);
		checkNotNull("type", type);
		this.source = source;
		this.typeNode = type;
	}
	
	public JType getTypeWithName(String simpleName){
		if (!isClass()) {
			throw new CodemuckerException("Type '%s' is not a class so can't search for type named '%s'",getSimpleName(), simpleName);
		}
		TypeDeclaration[] types = asType().getTypes();
		for (AbstractTypeDeclaration type : types) {
			if (simpleName.equals(type.getName().toString())) {
				return new JType(source, type);
			}
		}
		Collection<String> names = extractNames(types);
		throw new CodemuckerException("Can't find type named %s in %s. Found %s", simpleName, source, Arrays.toString(names.toArray()));
	}

	private static Collection<String> extractNames(TypeDeclaration[] types){
		Collection<String> names = new ArrayList<String>();
		for( AbstractTypeDeclaration type:types){
			names.add(type.getName().toString());
		}
		return names;
	}
	
	public List<JType> findChildTypesMatching(JTypeMatcher matcher){
		List<JType> found = new ArrayList<JType>();
		findChildTypesMatching(this, matcher, found);
		return found;
	}
	
	/* package */ void findChildTypesMatching(JTypeMatcher matcher, List<JType> found){
		findChildTypesMatching(this, matcher, found);
	}
	
	public List<JType> findAllChildTypes() {
		List<JType> found = new ArrayList<JType>();
		findChildTypesMatching(this, MATCH_ALL_TYPES, found);
		return found;
	}
	
	private void findChildTypesMatching(JType type, JTypeMatcher matcher, List<JType> found) {
		if (type.isClass()) {
			for (TypeDeclaration child : type.asType().getTypes()) {
				JType childJavaType = new JType(source, child);
				if (matcher.matches(childJavaType)) {
					found.add(childJavaType);
				}
				findChildTypesMatching(childJavaType, matcher, found);
			}
		}
	}
	
	public JTypeMutator asMutator(){
		return new JTypeMutator(this);
	}

	public List<JField> findFieldsMatching(Matcher<JField> matcher) {
		List<JField> found = newArrayList();
		findFieldsMatching(matcher, found);
		return found;
	}
	
	private void findFieldsMatching(final Matcher<JField> matcher, final List<JField> found) {
		int maxDepth = 0;
		final JType parent = this;
		BaseASTVisitor visitor = new IgnoreableChildTypesVisitor(maxDepth) {
			@Override
			public boolean visit(FieldDeclaration node) {
				JField field = new JField(parent, node);
				if (matcher.matches(field)) {
					found.add(field);
				}
				return super.visit(node);
			}
		};
		this.typeNode.accept(visitor);
	}
	
	public List<JMethod> findMethodsMatching(Matcher<JMethod> matcher) {
		List<JMethod> found = newArrayList();
		findMethodsMatching(matcher, found);
		return found;
	}

	public Collection<JMethod> getAllJavaMethods() {
		List<JMethod> found = newArrayList();
		findMethodsMatching(MATCH_ALL_METHODS, found);
		return found;
	}
	
	private void findMethodsMatching(final Matcher<JMethod> matcher, final List<JMethod> found) {
		int maxDepth = 0;
		final JType parent = this;
		BaseASTVisitor visitor = new IgnoreableChildTypesVisitor(maxDepth) {
			@Override
			public boolean visit(MethodDeclaration node) {
				JMethod javaMethod = new JMethod(parent, node);
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
				JMethod javaMethod = new JMethod(parent, node);
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

	public AbstractTypeDeclaration getTypeNode() {
    	return typeNode;
    }
	
	public JSource getSource() {
		return source;
	}

	protected AST getAst() {
		return typeNode.getAST();
	}

	public String getSimpleName(){
		return typeNode.getName().getIdentifier();
	}

	public String getPackageName(){
		return JavaNameUtil.getPackageFor(typeNode);
	}
	
	public boolean isAccess(JAccess access) {
		return getJavaModifiers().asAccess().equals(access);
	}
	
	@SuppressWarnings("unchecked")
    public JModifiers getJavaModifiers(){
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
		return isClass() && !asType().isInterface();
	}

	public boolean isInterface() {
		return isClass() && asType().isInterface();
	}

	public boolean isClass() {
		return typeNode instanceof TypeDeclaration;
	}

	public TypeDeclaration asType() {
		return (TypeDeclaration) typeNode;
	}

	public EnumDeclaration asEnum() {
		return (EnumDeclaration) typeNode;
	}

	public AnnotationTypeDeclaration asAnnotation() {
		return (AnnotationTypeDeclaration) typeNode;
	}

	public <A extends Annotation> boolean hasAnnotationOfType(Class<A> annotationClass,boolean includeChildClassesInLookup) {
		return getAnnotationOfType(annotationClass,includeChildClassesInLookup) != null;
	}

	public <A extends Annotation> JAnnotation getAnnotationOfType(Class<A> annotationClass, boolean includeChildClassesInLookup) {
		return JAnnotation.getAnnotationOfType(typeNode, includeChildClassesInLookup?JAnnotation.ANY_DEPTH:JAnnotation.DIRECT_DEPTH, annotationClass);
	}
	
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
