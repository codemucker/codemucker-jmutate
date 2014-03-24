package org.codemucker.jmutate.ast;

import static com.google.common.collect.Lists.newArrayList;
import static org.codemucker.lang.Check.checkNotNull;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.finder.FindResult;
import org.codemucker.jmutate.ast.finder.FindResultImpl;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.transform.MutateContext;
import org.codemucker.jmutate.util.ClassUtil;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Lists;

/**
 * A convenience wrapper around an Ast java type
 */
public abstract class JType implements JAnnotatable, AstNodeProvider<ASTNode> {

	private final ASTNode typeNode;
	
	public static boolean isTypeNode(ASTNode node){
		return node instanceof AbstractTypeDeclaration || node instanceof AnonymousClassDeclaration;
	}
	
	/**
	 * If the node is a type node then return a new JType, else throw an exception
	 * @param node
	 * @return
	 */
	public static JType from(ASTNode node){
		if(node instanceof AbstractTypeDeclaration){
			return from((AbstractTypeDeclaration)node);
		}
		if(node instanceof AnonymousClassDeclaration){
			return from((AnonymousClassDeclaration)node);
		}
		throw new IllegalArgumentException(String.format("Expect either a %s or a %s but was %s",
			AbstractTypeDeclaration.class.getName(),
			AnonymousClassDeclaration.class.getName(), 
			node.getClass().getName()
		));
	}
	
	public static JType from(AbstractTypeDeclaration node){
		return new AbstractTypeJType(node);
	}
	
	public static JType from(AnonymousClassDeclaration node){
		return new AnonynousClassJType(node);
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
	public JType getDirectChildTypeWithName(String simpleName){
		if (!isClass()) {
			throw new MutateException("Type '%s' is not a class so can't search for type named '%s'",getSimpleName(), simpleName);
		}
		TypeDeclaration[] types = asTypeDecl().getTypes();
		for (AbstractTypeDeclaration type : types) {
			if (simpleName.equals(type.getName().toString())) {
				return JType.from(type);
			}
		}
		Collection<String> names = extractTypeNames(types);
		throw new MutateException("Can't find type named %s in %s. Found %s", simpleName, this, Arrays.toString(names.toArray()));
	}

	private static Collection<String> extractTypeNames(TypeDeclaration[] types){
		Collection<String> names = newArrayList();
		for( AbstractTypeDeclaration type:types){
			names.add(type.getName().toString());
		}
		return names;
	}

	public FindResult<JField> findAllFields(){
		return findFieldsMatching(AJField.any());
	}
	
	public FindResult<JField> findFieldsMatching(final Matcher<JField> matcher) {
		final Collection<JField> found = Lists.newArrayList();
		//use visitor as anonymous class does not contain a fields property 
		ASTVisitor visitor = new BaseASTVisitor() {
			@Override
			protected boolean visitNode(ASTNode node) {
				//skip child types
				if(isTypeNode(node)){
					return false;
				} else if( node instanceof FieldDeclaration){
					JField field = JField.from((FieldDeclaration)node);
					if( matcher.matches(field)) {
						found.add(field);
					}
					return false;
				} 
				return true;
			}
		};
		visitChildren(visitor);
		return FindResultImpl.from(found);
	}
			
	/**
	 * Find all top level methods declared on this type. Ignores methods 
	 * defined in internal types or anonymous classes
	 * 
	 * @param matcher
	 * @return
	 */
	public FindResult<JMethod> findAllJMethods() {
		return findMethodsMatching(AJMethod.any());		
	}
	
	/**
	 * Find all top level methods declared on this type which match the given matcher. Ignores methods 
	 * defined in internal types or anonymous classes
	 * 
	 * @param matcher
	 * @return
	 */
	public FindResult<JMethod> findMethodsMatching(final Matcher<JMethod> matcher) {
		final List<JMethod> found = newArrayList();
		//use a visitor as the anonymous type does not have a 'methods' field
		ASTVisitor visitor = new BaseASTVisitor(){
			@Override
			protected boolean visitNode(ASTNode node) {
				//ignore child types
				if(isTypeNode(node)){
					return false;
				}
				if(JMethod.isMethodNode(node)){
					JMethod m = JMethod.from(node);
					if(matcher.matches(m)){
						found.add(m);
					}
					return false;//no need to go deeper
				}
				return true;
			}
		};
		visitChildren(visitor);
		return FindResultImpl.from(found);
	}
	
	/**
	 * Determine if there are any top level methods matching the given matcher
	 * 
	 * @param matcher
	 * @return
	 */
	public boolean hasMethodsMatching(final Matcher<JMethod> matcher) {
		final AtomicBoolean foundReturn = new AtomicBoolean();
		ASTVisitor visitor = new BaseASTVisitor() {
			boolean found = false;
			@Override
			public boolean visitNode(ASTNode node) {
				if(found){//finished looking
					return false;
				}
				if(JType.isTypeNode(node)){ //ignore child types
					return false;
				}
				if(JMethod.isMethodNode(node)){
					JMethod method = JMethod.from(node);
					if (matcher.matches(method)) {
						foundReturn.set(true);
						found = true;
						return false;//don't descend
					}
				}
				return true;
			}
		};
		visitChildren(visitor);
		return foundReturn.get();
	}
	
	public FindResult<JType> findDirectChildTypes(){
		return findDirectChildTypesMatching(AJType.any());
	}
	
	/**
	 * Find direct child types which match the given matcher. This is not recursive.
	 */
	public FindResult<JType> findDirectChildTypesMatching(final Matcher<JType> matcher){
		final List<JType> found = Lists.newArrayList();
		ASTVisitor visitor = new BaseASTVisitor(){
			@Override
			public boolean visitNode(ASTNode node) {
				if(JType.isTypeNode(node)){
					JType t = JType.from(node);
					if(matcher.matches(t)){
						found.add(t);	
					}
					return false;//don't walk child types
				}
				if(JMethod.isMethodNode(node)){
					return false;//don't walk methods
				}
				return true;//walk everything else
			}
		};
		visitChildren(visitor);
		return FindResultImpl.from(found);
	}
	/**
	 * Recursively find all child types
	 */
	public FindResult<JType> findAllChildTypes() {
		return findChildTypesMatching(AJType.any());
	}
	
	/**
	 * Recursively find all child types matching the given matcher. This includes internal anonymous
	 * types, and types within types
	 */
	public FindResult<JType> findChildTypesMatching(final Matcher<JType> matcher){
		final Collection<JType> found = Lists.newArrayList();
		ASTVisitor visitor = new BaseASTVisitor() {
			@Override
			protected boolean visitNode(ASTNode node) {
				if(JType.isTypeNode(node)){
					JType type = JType.from(node);
					if(matcher.matches(type)) {
						found.add(type);
					}
				}
				return true;
			}
		};
		visitChildren(visitor);
		return FindResultImpl.from(found);
	}
	
	@SuppressWarnings("unchecked")
	private void visitChildren(final ASTVisitor visitor){
		List<ASTNode> bodyNodes;
		if( typeNode instanceof AbstractTypeDeclaration){
			bodyNodes = ((AbstractTypeDeclaration)typeNode).bodyDeclarations();
		} else {
			bodyNodes = ((AnonymousClassDeclaration)typeNode).bodyDeclarations();
		}
		for(ASTNode node:bodyNodes){
			node.accept(visitor);
		}
	}

	public AST getAst() {
		return typeNode.getAST();
	}

	public JCompilationUnit getJCompilationUnit(){
		return JCompilationUnit.from(getCompilationUnit());
	}
	
	public CompilationUnit getCompilationUnit(){
		ASTNode parent = typeNode;
		while( parent != null){
			if( parent instanceof CompilationUnit){
				return (CompilationUnit)parent;
			}
			parent = parent.getParent();			
		}
		throw new MutateException("Couldn't find compilation unit. Unexpected");
	}

	public String getPackageName(){
		return JavaNameUtil.getPackageFor(typeNode);
	}

	public boolean isAccess(JAccess access) {
		return getModifiers().asAccess().equals(access);
	}

	public boolean isAnonymousClass() {
		return typeNode instanceof AnonymousClassDeclaration;
	}
	
	public boolean isAbstract() {
		return getModifiers().isAbstract();
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

	public JTypeMutator asMutator(MutateContext ctxt){
		return new JTypeMutator(ctxt, this);
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
	
	public boolean isSubClassOf(Class<?> superClass) {
		String qualifiedName = JavaNameUtil.compiledNameToSourceName(superClass.getName());
		return isSubClassOf(superClass, qualifiedName);
	}
	
	public boolean isSubClassOf(String genericParentQualifiedName) {
		return isSubClassOf(null, genericParentQualifiedName);
	}
	
	public boolean isSubClassOf(Class<?> superClass,String genericSuperClassName) {
		String rawSuperClassName = JavaNameUtil.removeGenericPart(genericSuperClassName);
		FindResult<JType> allTypesInCu = getJCompilationUnit().findAllTypes();
		//TODO:this can be done better. Really need to look up on each type
		//and keep rack what has been tested already
		Map<String,JType> fullNameToType = new HashMap<String, JType>();
		
		for(JType t : allTypesInCu){
			fullNameToType.put(t.getFullName(),t);
		}
		//if the required super class is in the compilation unit, we can do all of it in here
		//without having to load anything
		if(isSubClassOf(genericSuperClassName, rawSuperClassName, fullNameToType)){
			return true;
		}
		//let's fall back to proper class loading
		superClass = superClass==null?ClassUtil.loadClassOrNull(rawSuperClassName):superClass;
		if( superClass != null)
		{
			//lets try loading this class directly. If it's not part of a snippet or code gen, then
			//we might be successful
			Class<?> thisClass = ClassUtil.loadClassOrNull(getFullName());
			
			if(thisClass != null && superClass != null){
				return superClass.isAssignableFrom(thisClass);
			}
	
			//ok, now we have to see if any imports which this class directly or indirectly extends
			//is a subclass.
			Collection<Type> extendTypes = findImmediateSuperClassesAndInterfaceTypes();
			//only try external class
			for(Type t : extendTypes){
				String fn = JavaNameUtil.resolveQualifiedName(t);
				if(!fullNameToType.containsKey(fn)){
					//external class, lets try it
					Class<?> external = ClassUtil.loadClassOrNull(fn);
					if( external != null && superClass.isAssignableFrom(external)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	///TODO! !!!!!!! this all needs a serious look at
	private boolean isSubClassOf(String genericParentName, String rawParentName, Map<String,JType> fullNameToType) {
		Collection<Type> extendTypes = findImmediateSuperClassesAndInterfaceTypes();
		for (Type type : extendTypes) {
			if(!type.isWildcardType()){
				String fqn = JavaNameUtil.resolveQualifiedName(type);
				String rawFqn = JavaNameUtil.removeGenericPart(fqn);
				
				if(rawFqn.equals(rawParentName)){
					return true;
				}
				if(typeExtends(type, genericParentName)){
					return true;
				}
				//let's see if the parent internal type implements this
				//TODO:we could cache this result
				JType withinCuSuperclass = fullNameToType.get(fqn);
				
				if(withinCuSuperclass != null && withinCuSuperclass.isSubClassOf(genericParentName,rawParentName,fullNameToType)){
					return true;
				}
			}
		}
		return false;
	}

	///TODO! !!!!!!! this all needs a serious look at
	private boolean typeExtends(Type type,String rawSuperclass) {
		try {
			if( type.resolveBinding() != null && typeExtends(type.resolveBinding(), rawSuperclass)){
				return true;
			}
		} catch(IllegalStateException e){
			throw new IllegalStateException("error while resolving " + type, e);
		}
		return false;
	}
	
	private static boolean typeExtends(ITypeBinding type, String fullName){
		//System.out.println(type.getQualifiedName() + "==?" + fullName);
		if(fullName.equals(type.getQualifiedName())){
			return true;
		}
		ITypeBinding superType = type.getSuperclass();
		if(superType != null && typeExtends(superType, fullName)){
			return true;
		}
		for(ITypeBinding interfaceType:type.getInterfaces()){
			if(typeExtends(interfaceType, fullName)) {
				return true;
			}
		}
		return false;
	}
	
	protected abstract Collection<Type> findImmediateSuperClassesAndInterfaceTypes();
	
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
			return JavaNameUtil.resolveQualifiedName(typeNode);
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
		
		@SuppressWarnings("unchecked")
		@Override
		protected Collection<Type> findImmediateSuperClassesAndInterfaceTypes() {
			Collection<Type> types = Lists.newArrayList();
			if( isConcreteClass()){
				TypeDeclaration type = asTypeDecl();
				if( type.getSuperclassType() != null){
					types.add(type.getSuperclassType());
//					if( type.getSuperclassType().resolveBinding()!= null){
//						for(ITypeBinding bind: type.getSuperclassType().resolveBinding().getDeclaredTypes()){
//							bind.
//						}
//					}
				}
				types.addAll(type.superInterfaceTypes());	
			}
			return types;
		}
	}
	
	//TODO:count occurrances in the source file
	public static class AnonynousClassJType extends JType {
		private static final List<IExtendedModifier> modifiers = Collections.emptyList();
		
		private final AnonymousClassDeclaration typeNode;
		
		public AnonynousClassJType(AnonymousClassDeclaration type) {
			super(type);
			this.typeNode = type;
		}
		
		public String getFullName(){
			return findParentType().getFullName() + "." + extractAnonymousClassNumber();//typeNode.getName().getIdentifier();
		}
		
		public String getSimpleName(){
			return findParentType().getSimpleName() + "." + extractAnonymousClassNumber();//typeNode.getName().getIdentifier();
		}
		
		private JType findParentType(){
			ASTNode parent = typeNode;
			while( parent != null && !(parent instanceof CompilationUnit)){
				if( parent instanceof AbstractTypeDeclaration ){
					return JType.from((AbstractTypeDeclaration) parent);
				}
				parent = parent.getParent();
			}
			throw new MutateException("couldn't find parent type");
		}
		
		private int extractAnonymousClassNumber(){
			final String propName = "codemucker.anon.class.num";
			Integer num = (Integer) typeNode.getProperty(propName);
			if(num == null){
				BaseASTVisitor visitor = new BaseASTVisitor(){
					int count = 0;
					@Override
					public boolean visit(AnonymousClassDeclaration node) {
						node.setProperty(propName, count++);
						return super.visit(node);
					}
				};
				getCompilationUnit().accept(visitor);
				num = (Integer) typeNode.getProperty(propName);
			}
			return num;
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
		
		@SuppressWarnings("unchecked")
		@Override
		protected Collection<Type> findImmediateSuperClassesAndInterfaceTypes() {
			Collection<Type> types = Lists.newArrayList();
			if( isConcreteClass()){
				TypeDeclaration type = asTypeDecl();
				if( type.getSuperclassType() != null){
					types.add(type.getSuperclassType());
				}
				types.addAll(type.superInterfaceTypes());	
			}
			return types;
		}
	}
}
