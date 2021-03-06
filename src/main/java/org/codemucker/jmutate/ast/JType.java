package org.codemucker.jmutate.ast;

import static com.google.common.collect.Lists.newArrayList;
import static org.codemucker.lang.Check.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.tools.ant.taskdefs.optional.javah.Kaffeh;
import org.codemucker.jfind.DefaultFindResult;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.PredicateToFindFilterAdapter;
import org.codemucker.jfind.SearchScope;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * A convenience wrapper around an Ast java type
 */
public abstract class JType implements AnnotationsProvider, AstNodeProvider<ASTNode> {

	private final ASTNode typeNode;
	
	//e.g. S extends Foo<S>
	//TODO: S extends Foo<S,T>
	private static final Pattern RE_EXTRACT_SELF_PARAM = Pattern.compile("^(\\w+) extends (\\w+)<\\1>$");
			
	public static boolean isTypeNode(ASTNode node){
		return node instanceof AbstractTypeDeclaration || node instanceof AnonymousClassDeclaration;
	}
	
	/**
	 * If the node is a type node then return a new JType, else throw an exception
	 * @param node
	 * @return
	 */
	public static JType from(ASTNode node){
		JType t = fromOrNull(node);
		if(t == null){
			throw new IllegalArgumentException(String.format("Expect either a %s or a %s but was %s",
				AbstractTypeDeclaration.class.getName(),
				AnonymousClassDeclaration.class.getName(), 
				node.getClass().getName()
			));
		}
		return t;
	}
	
	private static JType fromOrNull(ASTNode node){
		if(node instanceof AbstractTypeDeclaration){
			return from((AbstractTypeDeclaration)node);
		}
		if(node instanceof AnonymousClassDeclaration){
			return from((AnonymousClassDeclaration)node);
		}
		return null;
	}
	
	public static JType from(AbstractTypeDeclaration node){
		return new AbstractTypeJType(node);
	}
	
	public static JType from(AnonymousClassDeclaration node){
		return new AnonynousClassJType(node);
	}

	/**
	 * Find the source for the given type. Handles innder classes too
	 * @param fullName the compiled class name
	 * @param ctxt
	 * @return
	 */
	public static JType fromClassNameOrNull(String fullName,JMutateContext ctxt){
		//TODO:move this into a central indexer where it can cache these results
		int inner = fullName.indexOf('$');
		String sourcePath;
		if(inner !=-1){
			sourcePath = fullName.substring(0,inner);
		} else {
			sourcePath = fullName;
		}
		sourcePath = sourcePath.replace('.', '/') + ".java";
		
		JSourceFile source = ctxt.getSourceLoader().loadSourceForClass(fullName);
		if(source != null){
			source.getMainType().findTypesMatching(AJType.with().fullName(AString.equalTo(NameUtil.compiledNameToSourceName(fullName)))).getFirstOrNull();
		}
		return null;
	}
	
	public static boolean is(ASTNode node){
	    return node instanceof AbstractTypeDeclaration || node instanceof AnonymousClassDeclaration || node instanceof AnnotationTypeDeclaration;
	}
	
	private JType(ASTNode type) {
		checkNotNull("type", type);
		this.typeNode = type;
	}
	

	/**
	 * Return the full name along with the generic parts (if any)
	 * 
	 * 
	 * @return
	 */
	public String getFullNameGeneric(){
		List<TypeParameter> types = this.findGenericTypes().toList();
		if(!types.isEmpty()){
			StringBuilder sb = new StringBuilder(getFullName());
			sb.append("<");
			boolean comma = false;
			for(TypeParameter t :types){
				if(comma){
					sb.append(",");
				}
				sb.append(NameUtil.resolveQualifiedNameElseShort(t.getName()));
				comma=true;
			}
			sb.append(">");
			return sb.toString();
		} else {
			return getFullName();
		}
	}
	
	public String getTypeBoundsExpressionOrNull(){
		
		ASTNode node = getAstNode();
		if( node instanceof TypeDeclaration){
			TypeDeclaration t = (TypeDeclaration)node;
			List<TypeParameter> typeParams = t.typeParameters();
			if(!typeParams.isEmpty()){
				StringBuilder sb = new StringBuilder();
				JAstFlattener flattener = new JAstFlattener(sb);
				sb.append("<");
				boolean comma = false;
				for(TypeParameter p:typeParams){
					if(comma){
						sb.append(',');
					}
					p.accept(flattener);
					comma = true;
				}
				sb.append(">");
				return sb.toString();
			}
		}
		return null;
	}
		
	public abstract String getFullName();
	//TODO:rename to 'shortName'? to avoi ambiguity with dom SimpleName?
	public abstract String getSimpleName();
	public abstract JModifier getModifiers();
    public abstract List<ASTNode> getBodyDeclarations();
	public abstract boolean isInnerClass();
	public abstract boolean isTopLevelClass();

	@Override
	public ASTNode getAstNode(){
		return typeNode;
	}
	
	public FindResult<TypeParameter> findGenericTypes(){
		return findGenericTypes(Logical.<TypeParameter>any());
	}
	
	public FindResult<TypeParameter> findGenericTypes(final Predicate<TypeParameter> predicate){
		return findGenericTypes(PredicateToFindFilterAdapter.from(predicate));
	}
	
	public FindResult<TypeParameter> findGenericTypes(final Matcher<TypeParameter> matcher){
		final List<TypeParameter> found = Lists.newArrayList();
		
		ASTVisitor visitor = new BaseASTVisitor(){
			
			private boolean visit = true;
			@Override
			protected boolean visitNode(ASTNode node) {
				if(!visit){
					return false;
				}
				if( node instanceof MethodDeclaration || node instanceof FieldDeclaration){
					visit = false;
					return false;
				}
				if( node instanceof TypeParameter){
					TypeParameter typeParam = (TypeParameter)node;
					if(matcher.matches(typeParam)){
						found.add(typeParam);
					}
				}
				//System.out.println("visit" + node.getClass() + "  " + trim(node.toString()));
				
				return true;
			}
			
			@Override
			public void endVisitNode(ASTNode node) {
			//	System.out.println("end visit" + node.getClass() + "  " + trim(node.toString()));
			}
			
		};
		getAstNode().accept(visitor);
		return DefaultFindResult.from(found);
	}

	/**
	 * Find a child type with the given simple name, or throw an exception if no child type with that name
	 */
	public JType getChildTypeWithName(String simpleName){
		JType found = getChildTypeWithNameOrNull(simpleName);
		if(found!=null){
			return found;
		}
		Collection<String> names = extractTypeNames(asTypeDecl().getTypes());
		throw new JMutateException("Can't find type named %s in %s. Found %s", simpleName, this, Arrays.toString(names.toArray()));
	}

	public JType getChildTypeWithNameOrNull(String simpleName){
		if (!isClass()) {
			throw new JMutateException("Type '%s' is not a class so can't search for type named '%s'",getSimpleName(), simpleName);
		}
		TypeDeclaration[] types = asTypeDecl().getTypes();
		for (AbstractTypeDeclaration type : types) {
			if (simpleName.equals(type.getName().toString())) {
				return JType.from(type);
			}
		}
		return null;
	}
	
	private static Collection<String> extractTypeNames(TypeDeclaration[] types){
		Collection<String> names = newArrayList();
		for( AbstractTypeDeclaration type:types){
			names.add(type.getName().toString());
		}
		return names;
	}

	public FindResult<JField> findFields(){
		return findFieldsMatching(AJField.any(),SearchScope.DIRECT);
	}
	
	public FindResult<JField> findNestedFields(){
		return findFieldsMatching(AJField.any(),SearchScope.DIRECT_AND_CHILDREN);
	}
	
	public FindResult<JField> findFieldsMatching(final Matcher<JField> matcher) {
		return findFieldsMatching(matcher, SearchScope.DIRECT);
	}
	
	public FindResult<JField> findNestedFieldsMatching(final Matcher<JField> matcher) {
		return findFieldsMatching(matcher, SearchScope.DIRECT_AND_CHILDREN);
	}
	
	public FindResult<JField> findFieldsMatching(final Matcher<JField> matcher,final SearchScope scope) {
		List<JField> collected = Lists.newArrayList();
		collectFieldsMatching(collected, matcher, scope);
		return DefaultFindResult.from(collected);
	}
	
	private void collectFieldsMatching(final List<JField> collected, final Matcher<JField> matcher,final SearchScope scope) {
		//use visitor as anonymous class does not contain a fields property 
		ASTVisitor visitor = new BaseASTVisitor() {
			@Override
			protected boolean visitNode(ASTNode node) {
				//skip child types
				if(!SearchScope.CHILDREN.isSet(scope) && isTypeNode(node)){
					return false;
				} else if(node instanceof FieldDeclaration){
					JField field = JField.from((FieldDeclaration)node);
					if(matcher.matches(field)) {
						collected.add(field);
					}
					return false;
				} 
				return true;
			}
		};
		visitChildren(visitor);
		
		if (SearchScope.PARENT.isSet(scope)) {
			JType superType = JType.this.getSuperTypeOrNull();
			if (superType != null) {
				superType.collectFieldsMatching(collected, matcher, scope.not(SearchScope.CHILDREN));
			}
		}
	}
	
	/**
	 * Find all top level methods declared on this type. Ignores methods 
	 * defined in internal types or anonymous classes
	 * 
	 * @param matcher
	 * @return
	 */
	public FindResult<JMethod> findMethods() {
		return findMethodsMatching(AJMethod.any(),SearchScope.DIRECT);		
	}
	
	public FindResult<JMethod> findNestedMethods() {
		return findMethodsMatching(AJMethod.any(),SearchScope.DIRECT_AND_CHILDREN);		
	}
	
	/**
	 * Find all top level methods declared on this type which match the given matcher. Ignores methods 
	 * defined in internal types or anonymous classes
	 * 
	 * @param matcher
	 * @return
	 */
	public FindResult<JMethod> findMethodsMatching(final Matcher<JMethod> matcher) {
		return findMethodsMatching(matcher,SearchScope.DIRECT);
	}
	
	public FindResult<JMethod> findNestedMethodsMatching(final Matcher<JMethod> matcher) {
		return findMethodsMatching(matcher,SearchScope.DIRECT_AND_CHILDREN);
	}
	
	public FindResult<JMethod> findMethodsMatching(final Matcher<JMethod> matcher, final SearchScope scope) {
		List<JMethod> collected = newArrayList();
		collectMethodsMatching(collected,matcher,scope);
		return DefaultFindResult.from(collected);
		
	}
	private void collectMethodsMatching(final List<JMethod> collected,final Matcher<JMethod> matcher, final SearchScope scope) {
		//use a visitor as the anonymous type does not have a 'methods' field
		ASTVisitor visitor = new BaseASTVisitor(){
			@Override
			protected boolean visitNode(ASTNode node) {
				//ignore child types
				if(!SearchScope.CHILDREN.isSet(scope)&& isTypeNode(node)){
					return false;
				}
				if(JMethod.isMethodNode(node)){
					JMethod m = JMethod.from(node);
					if(matcher.matches(m)){
						collected.add(m);
					}
					return false;//no need to go deeper
				}
				return true;
			}
		};
		visitChildren(visitor);
		
		if(SearchScope.PARENT.isSet(scope)) {
			JType superType = JType.this.getSuperTypeOrNull();
			if (superType != null) {
				superType.collectMethodsMatching(collected, matcher, scope.not(SearchScope.CHILDREN));
			}
		}
	}
	

	/**
	 * Determine if there are any top level methods matching the given matcher
	 * 
	 * @param matcher
	 * @return
	 */
	public boolean hasMethodMatching(final Matcher<JMethod> matcher) {
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
	
	public FindResult<JType> findChildTypes(){
		return findTypesMatching(AJType.any(),SearchScope.DIRECT);
	}
	
	/**
	 * Find direct child types which match the given matcher. This is not recursive.
	 */
	public FindResult<JType> findTypesMatching(final Matcher<JType> matcher){
		return findTypesMatching(matcher, SearchScope.DIRECT);
	}
	
	/**
	 * Recursively find all child types
	 */
	public FindResult<JType> findNestedTypes() {
		return findTypesMatching(AJType.any(),SearchScope.DIRECT_AND_CHILDREN);
	}
	
	/**
	 * Recursively find all child types matching the given matcher. This includes internal anonymous
	 * types, and types within types
	 */
	public FindResult<JType> findNestedTypesMatching(final Matcher<JType> matcher){
		return findTypesMatching(matcher,SearchScope.DIRECT_AND_CHILDREN);
	}
	
	public FindResult<JType> findTypesMatching(final Matcher<JType> matcher, SearchScope scope){
		List<JType> collected = Lists.newArrayList();
		collectTypesMatching(collected, matcher, scope);
		return DefaultFindResult.from(collected);
	}
	
	private void collectTypesMatching(final List<JType> collected, final Matcher<JType> matcher, SearchScope scope){
		final boolean walkChildren = SearchScope.CHILDREN.isSet(scope);
		ASTVisitor visitor = new BaseASTVisitor(){
			@Override
			public boolean visitNode(ASTNode node) {
				if(JType.isTypeNode(node)){
					JType t = JType.from(node);
					if(matcher.matches(t)){
						collected.add(t);	
					}
					return walkChildren;
				}
				if(JMethod.isMethodNode(node)){
					return walkChildren;
				}
				return true;//walk everything else
			}
		};
		visitChildren(visitor);
		
		if (SearchScope.PARENT.isSet(scope)) {
			JType superType = JType.this.getSuperTypeOrNull();
			if (superType != null) {
				superType.collectTypesMatching(collected, matcher, scope.not(SearchScope.CHILDREN));
			}
		}
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

	public JType getSuperTypeOrNull(){
		JType parentType = null;
		String superType = getSuperTypeFullName();
		if (superType != null) {
			parentType = getCompilationUnit().getSourceLoader().loadTypeForClass(superType);
		}
		return parentType;
	}
	
	/**
	 * Return the super class full type name, or if none is present (for interfaces, enums, annotations) just return null
	 * @return
	 */
	public String getSuperTypeFullName() {
		if (typeNode instanceof TypeDeclaration) {
			Type st = ((TypeDeclaration) typeNode).getSuperclassType();
			if (st != null) {
				String fn = NameUtil.resolveQualifiedName(st);
				return Object.class.getName().equals(fn)?null:fn;
			}
		}
		return null;
	}

	public JCompilationUnit getCompilationUnit(){
		return JCompilationUnit.findCompilationUnit(getAstNode());
	}

	/**
	 * Find the immediate parent type, or null if none. The parent is parent _node_, not the parent
	 * type
	 * @return
	 */
	public JType getParentJType() {
		ASTNode parent = typeNode.getParent();
		while( parent != null){
			JType t = fromOrNull(parent);
			if(t != null){
				return t;
			}
			parent = parent.getParent();
		}
		return null;
	}
	
	public String getPackageName(){
		PackageDeclaration pkg = getCompilationUnit().getAstNode().getPackage();
		if( pkg == null){
			return null;
		}
		return pkg.getName().getFullyQualifiedName();
	}

	public int getNumEnclosingClasses() {
		int count = 0;
		ASTNode node = getAstNode().getParent();
		while(node != null){
			if(is(node)){
				count++;
			}
			node = node.getParent();
		}
		return count;
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
	
	public boolean isNotAbstract() {
		return getModifiers().isAbstract(false);
	}
	
	public boolean isEnum() {
		return typeNode instanceof EnumDeclaration;
	}

	public boolean isAnnotation() {
		return typeNode instanceof AnnotationTypeDeclaration;
	}

	public boolean isConcreteClass() {
		return isClass() && !isAbstract() && !isInterface();
	}

	public boolean isInterface() {
		return isClass() && asTypeDecl().isInterface();
	}

	public boolean isClass() {
		return typeNode instanceof TypeDeclaration;
	}

	public JTypeMutator asMutator(JMutateContext ctxt){
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

	/**
	 * Returns the name of the generic type param which is used to reference self, or null if none.
	 * 
	 * E.g. in 
	 * 
	 * MyClass&lt;S extends MyClass&lt;S&gt;&gt;{}
	 * 
	 * returns 'S'
	 * 
	 * MyClass&lt;S&gt;{}
	 * 
	 * returns null
	 * 
	 * 
	 * @return
	 */
	public String getSelfTypeGenericParam() {
		//e.g. S extends Foo<S>
		List<TypeParameter> types = this.findGenericTypes().toList();
		if(types.isEmpty()){
			return null;
		}
		if(types.size()==1){
			String thisFullName = getFullName();
			String thisShortName = getSimpleName();
			
			for(TypeParameter t :types){
				java.util.regex.Matcher m = RE_EXTRACT_SELF_PARAM.matcher(t.toString());
				if(m.matches()){
					String selfName = m.group(1);
					String className = m.group(2);
					if(className.equals(thisFullName)||className.equals(thisShortName)){
						return selfName;
					}
				}
			}
		} else if (types.size() > 1){
			//e.g. T,S extends Foo<T,S> or S extends Foo<S,T>,T
			List<String> names = new ArrayList<>();
			StringBuilder sb = new StringBuilder(getSimpleName());
			sb.append("<");
			boolean comma = false;
			for(TypeParameter t :types){
				String name = t.getName().getIdentifier();
				names.add(name);
				if(comma){
					sb.append(",");
				}
				sb.append(name);
				comma=true;
			}
			sb.append(">");
			String lookFor = " extends " + sb.toString();//eg Foo<S,T,Z>
			for(TypeParameter t :types){
				if( t.toString().equals(t.getName().getIdentifier() + lookFor)){
					return t.getName().getIdentifier();
				}
			}
		}
		return null;
	}
	
	public boolean isSubClassOf(Class<?> superClass) {
		String qualifiedName = NameUtil.compiledNameToSourceName(superClass.getName());
		return isSubClassOf(superClass, qualifiedName);
	}
	
	public boolean isSubClassOf(String genericParentQualifiedName) {
		return isSubClassOf(null, genericParentQualifiedName);
	}
	
	public boolean isSubClassOf(Class<?> superClass,String genericSuperClassName) {
		String rawSuperClassName = NameUtil.removeGenericOrArrayPart(genericSuperClassName);
		FindResult<JType> allTypesInCu = getCompilationUnit().findAllTypes();
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
		superClass = superClass==null?MutateUtil.loadClassOrNull(rawSuperClassName):superClass;
		if( superClass != null)
		{
			//lets try loading this class directly. If it's not part of a snippet or code gen, then
			//we might be successful
			Class<?> thisClass = MutateUtil.loadClassOrNull(getFullName());
			
			if(thisClass != null && superClass != null){
				return superClass.isAssignableFrom(thisClass);
			}
	
			//ok, now we have to see if any imports which this class directly or indirectly extends
			//is a subclass.
			Collection<Type> extendTypes = findImmediateSuperClassesAndInterfaceTypes();
			//only try external class
			for(Type t : extendTypes){
				String fn = NameUtil.resolveQualifiedName(t);
				if(!fullNameToType.containsKey(fn)){
					//external class, lets try it
					Class<?> external = MutateUtil.loadClassOrNull(fn);
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
				String fqn = NameUtil.resolveQualifiedName(type);
				String rawFqn = NameUtil.removeGenericOrArrayPart(fqn);
				
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
		
		private final AbstractAnnotations annotable = new AbstractAnnotations() {
	        @Override
	        public ASTNode getAstNode() {
	            return typeNode;
	        }

	        @SuppressWarnings("unchecked")
	        @Override
	        protected List<IExtendedModifier> getModifiers() {
	            return typeNode.modifiers();
	        }
	    };
	    
		public AbstractTypeJType(AbstractTypeDeclaration type) {
			super(type);
			this.typeNode = type;
		}

		@Override
        public Annotations getAnnotations() {
            return annotable;
        }
		
		public String getFullName(){
			return NameUtil.resolveQualifiedName(typeNode);
		}
		
		public String getSimpleName(){
			return typeNode.getName().getIdentifier();
		}

		@SuppressWarnings("unchecked")
	    public List<ASTNode> getBodyDeclarations() {
		    return typeNode.bodyDeclarations();
	    }
		
		@SuppressWarnings("unchecked")
	    public JModifier getModifiers(){
			return new JModifier(typeNode.getAST(),typeNode.modifiers());
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

	//TODO:count occurrances in the source file to calculate anonymuous class number
	public static class AnonynousClassJType extends JType 
	{
		private static final List<IExtendedModifier> modifiers = Collections.emptyList();
		
		private final AnonymousClassDeclaration typeNode;
		
        private final AbstractAnnotations annotable = new AbstractAnnotations() {
            @Override
            public ASTNode getAstNode() {
                return typeNode;
            }

            @Override
            protected List<IExtendedModifier> getModifiers() {
                return modifiers;
            }
        };

		public AnonynousClassJType(AnonymousClassDeclaration type) {
			super(type);
			this.typeNode = type;
		}
		
        @Override
        public Annotations getAnnotations() {
            return annotable;
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
			throw new JMutateException("couldn't find parent type");
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
				getCompilationUnit().getAstNode().accept(visitor);
				num = (Integer) typeNode.getProperty(propName);
			}
			return num;
		}
		
		@SuppressWarnings("unchecked")
	    public List<ASTNode> getBodyDeclarations() {
		    return typeNode.bodyDeclarations();
	    }
		
	    public JModifier getModifiers(){
			return new JModifier(typeNode.getAST(),modifiers);
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
