package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.FindResultImpl;
import com.bertvanbrakel.codemucker.ast.matcher.AField;
import com.bertvanbrakel.codemucker.ast.matcher.AMethod;
import com.bertvanbrakel.codemucker.ast.matcher.AType;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.util.JavaNameUtil;
import com.bertvanbrakel.test.finder.matcher.Matcher;

/**
 * A convenience wrapper around an Ast java type
 */
public abstract class JType implements JAnnotatable, AstNodeProvider<ASTNode> {

	private final ASTNode typeNode;
	
	public static JType from(AbstractTypeDeclaration type){
		return new AbstractTypeJType(type);
	}
	
	public static JType from(AnonymousClassDeclaration type){
		return new AnonynousClassJType(type);
	}
	
	protected JType(ASTNode type) {
		checkNotNull("type", type);
		this.typeNode = type;
	}
	
	public abstract String getFullName();
	public abstract String getSimpleName();
	public abstract JModifiers getModifiers();
    public abstract List<ASTNode> getBodyDeclarations();
	public abstract boolean isInnerClass();
	public abstract boolean isTopLevelClass();
	
	@Override
	public ASTNode getAstNode(){
		return typeNode;
	}
	
	/**
	 * Find a child type with the given simple name, or throw an exception if no child type with that name
	 */
	public JType getChildTypeWithName(String simpleName){
		if (!isClass()) {
			throw new CodemuckerException("Type '%s' is not a class so can't search for type named '%s'",getSimpleName(), simpleName);
		}
		TypeDeclaration[] types = asTypeDecl().getTypes();
		for (AbstractTypeDeclaration type : types) {
			if (simpleName.equals(type.getName().toString())) {
				return JType.from(type);
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

	public FindResult<JType> findDirectChildTypes(){
		return findDirectChildTypesMatching(AType.any());
	}
	
	/**
	 * Find direct child types which match the given matcher. THis is not recursive.
	 */
	public FindResult<JType> findDirectChildTypesMatching(Matcher<JType> matcher){
		List<JType> found = newArrayList();
		if( isClass()){
			for (TypeDeclaration child : asTypeDecl().getTypes()) {
				JType childJavaType = JType.from(child);
				if (matcher.matches(childJavaType)) {
					found.add(childJavaType);
				}
			}		
		}
		return FindResultImpl.from(found);
	}
	
	/**
	 * Recursively find all child types matching the given matcher
	 */
	public FindResult<JType> findChildTypesMatching(Matcher<JType> matcher){
		List<JType> found = newArrayList();
		findChildTypesMatching(this, matcher, found);
		return FindResultImpl.from(found);
	}
	
	/* package */ void findChildTypesMatching(Matcher<JType> matcher, List<JType> found){
		findChildTypesMatching(this, matcher, found);
	}
	
	/**
	 * Recursively find all child types
	 */
	public FindResult<JType> findAllChildTypes() {
		List<JType> found = newArrayList();
		findChildTypesMatching(this, AType.any(), found);
		return new FindResultImpl<JType>(found);
	}
	
	private void findChildTypesMatching(JType type, Matcher<JType> matcher, List<JType> found) {
		//collect
		NodeCollector collector = NodeCollector.newBuilder()
			.collectType(AnonymousClassDeclaration.class)
			//.collectType(AbstractTypeDeclaration.class)
			.collectType(EnumDeclaration.class)
			.collectType(AnnotationTypeDeclaration.class)
			.collectType(TypeDeclaration.class)
			.build();
		
		type.getAstNode().accept(collector);

		List<ASTNode> anons = collector.getCollectedAs();
		//convert and match
		for (ASTNode anon : anons) {
			JType child = null;
			if( anon instanceof AbstractTypeDeclaration){
				child = JType.from((AbstractTypeDeclaration)anon);
			} else if( anon instanceof AnonymousClassDeclaration){
				child = JType.from((AnonymousClassDeclaration)anon);
			}
			if( matcher.matches(child)){
				found.add(child);
			}
		}
	}
	
	public JTypeMutator asMutator(MutationContext ctxt){
		return new JTypeMutator(ctxt, this);
	}

	public FindResult<JField> findAllFields(){
		return findFieldsMatching(AField.any());
	}
	
	public FindResult<JField> findFieldsMatching(Matcher<JField> matcher) {
		List<JField> found = newArrayList();
		findFieldsMatching(matcher, found);
		return FindResultImpl.from(found);
	}
	
	private void findFieldsMatching(final Matcher<JField> matcher, final List<JField> found) {
		int maxDepth = 0;
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
		return FindResultImpl.from(found);
	}

	public FindResult<JMethod> findAllJMethods() {
		List<JMethod> found = newArrayList();
		findMethodsMatching(AMethod.any(), found);
		return FindResultImpl.from(found);
	}
	
	private void findMethodsMatching(final Matcher<JMethod> matcher, final Collection<JMethod> found) {
		int maxDepth = 0;
		BaseASTVisitor visitor = new IgnoreableChildTypesVisitor(maxDepth) {
			@Override
			public boolean visit(MethodDeclaration node) {
				if(super.visit(node)){
					JMethod javaMethod = new JMethod(node);
					if (matcher.matches(javaMethod)) {
						found.add(javaMethod);
					}
					return true;
				}
				return false;
			}
		};
		this.typeNode.accept(visitor);
	}
	
	public boolean hasMethodsMatching(final Matcher<JMethod> matcher) {
		int maxDepth = 0;
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
			if( parent instanceof CompilationUnit){
				return (CompilationUnit)parent;
			}
			parent = parent.getParent();			
		}
		throw new CodemuckerException("Couldn't find compilation unit. Unexpected");
	}

	public String getPackageName(){
		return JavaNameUtil.getPackageFor(typeNode);
	}

	public boolean isAnonymousClass() {
		return typeNode instanceof AnonymousClassDeclaration;
	}
	
	public boolean isAbstract() {
		return getModifiers().isAbstract();
	}
	
	public boolean isAccess(JAccess access) {
		return getModifiers().asAccess().equals(access);
	}
	
	public boolean isEnum() {
		return typeNode instanceof EnumDeclaration;
	}

	public boolean isAnnotation() {
		return typeNode instanceof AnnotationTypeDeclaration;
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

	public AbstractTypeDeclaration asAbstractTypeDecl() {
		return (AbstractTypeDeclaration) typeNode;
	}

	public EnumDeclaration asEnumDecl() {
		return (EnumDeclaration) typeNode;
	}

	public AnnotationTypeDeclaration asAnnotationDecl() {
		return (AnnotationTypeDeclaration) typeNode;
	}

	public AnonymousClassDeclaration asAnonymousClassDecl() {
		return (AnonymousClassDeclaration) typeNode;
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
	public List<org.eclipse.jdt.core.dom.Annotation> getAnnotations(){
		//all
		return getAnnotations(true);
	}
	
	/**
	 * Returns all the annotations attached to this class. This does not include annotation declarations, but rather use.
	 * @return
	 */
	public List<org.eclipse.jdt.core.dom.Annotation> getAnnotations(final boolean includeChildClassesInLookup){
		final int maxDepth = includeChildClassesInLookup?-1:0;
		return JAnnotation.findAnnotations(typeNode, maxDepth);
	}
	
	public abstract boolean isImplementing(Class<?> require);

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("JType [");
		getAstNode().accept(new JAstFlattener(sb));
		sb.append("]");
		return sb.toString();
	}

	public static class AbstractTypeJType extends JType {
		private final AbstractTypeDeclaration typeNode;
		
		public AbstractTypeJType(AbstractTypeDeclaration type) {
			super(type);
			this.typeNode = type;
		}
		
		public String getFullName(){
			return JavaNameUtil.getQualifiedNameFor(typeNode);
		}
		
		public String getSimpleName(){
			return typeNode.getName().getIdentifier();
		}
		
		@SuppressWarnings("unchecked")
	    public List<ASTNode> getBodyDeclarations() {
		    return typeNode.bodyDeclarations();
	    }
		
		@SuppressWarnings("unchecked")
	    public JModifiers getModifiers(){
			return new JModifiers(typeNode.getAST(),typeNode.modifiers());
		}

		public boolean isInnerClass() {
			return isClass() && typeNode.isMemberTypeDeclaration();
		}

		public boolean isTopLevelClass() {
			return typeNode.isPackageMemberTypeDeclaration();
		}
		
		public boolean isImplementing(Class<?> require) {
			String fullName = getFullName();
			try {
				Class<?> thisClass = Thread.currentThread().getContextClassLoader().loadClass(fullName);
				return require.isAssignableFrom(thisClass);
			} catch(ClassNotFoundException e){
				throw new CodemuckerException(String.format("Couldn't find compiled class for type '%s'", fullName),e);
			}
		}
	}
	
	public static class AnonynousClassJType extends JType {
		private static final List<IExtendedModifier> modifiers = Collections.emptyList();
		
		private final AnonymousClassDeclaration typeNode;
		
		public AnonynousClassJType(AnonymousClassDeclaration type) {
			super(type);
			this.typeNode = type;
		}
		
		public String getFullName(){
			return "";
		}
		
		public String getSimpleName(){
			return "";
		}
		
		@SuppressWarnings("unchecked")
	    public List<ASTNode> getBodyDeclarations() {
		    return typeNode.bodyDeclarations();
	    }
		
	    public JModifiers getModifiers(){
			return new JModifiers(typeNode.getAST(),modifiers);
		}

	    //TODO:should this be an inner class? is the idea the same?
		public boolean isInnerClass() {
			return false;
		}

		public boolean isTopLevelClass() {
			return false;
		}
		
		public boolean isImplementing(Class<?> require) {
		
			//TODO:look at extends...
			throw new UnsupportedOperationException("Don't support 'implements' for anonymous classes yet");
		}
	}

}
