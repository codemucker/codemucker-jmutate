package org.codemucker.jmutate.util;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.util.Collections;
import java.util.List;

import org.codemucker.jmutate.ast.MutateException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

public class JavaNameUtil {
	
	public static String removeGenericPart(String shortOrQualifiedName){
		int i = shortOrQualifiedName.indexOf('<');
		if( i != -1){
			shortOrQualifiedName = shortOrQualifiedName.substring(0,i);
		}
		return shortOrQualifiedName;
	}
	/**
	 * @see {link {@link #resolveQualifiedName(Type, StringBuilder)}. This just returns this as a string
	 * @param t
	 * @return
	 */
	public static String resolveQualifiedName(Type t){
		StringBuilder sb = new StringBuilder();
		resolveQualifiedName(t, sb);
		return sb.toString();
	}
	
	private static void resolveQualifiedName(Type t, StringBuilder sb){
		if(!tryResolveQualifiedName(t,sb)){
			throw new MutateException("Currently don't know how to handle type:" + t);
		}
	}
	/**
	 * Try all means possible to resolve the fully qualified name for the given type. Look in the source, try the classloader etc
	 * 
	 * @param t the type to resolve the fqn for
	 * @param sb append the result to this
	 */
	private static boolean tryResolveQualifiedName(Type t, StringBuilder sb){
		if(t.resolveBinding() != null ){
			sb.append(compiledNameToSourceName(t.resolveBinding().getQualifiedName()));
			return true;
		} else if (t.isPrimitiveType()) {
			sb.append(((PrimitiveType) t).getPrimitiveTypeCode().toString());
			return true;
		} else if (t.isSimpleType()) {
			SimpleType st = (SimpleType) t;
			String fqn = resolveQualifiedNameOrNull(st.getName());
			if( fqn != null){
				sb.append(fqn);
				return true;
			}
		} else if (t.isQualifiedType()) {
			QualifiedType qt = (QualifiedType) t;
			sb.append(resolveQualifiedName(qt.getName()));
			return true;
		} else if (t.isArrayType()) {
			ArrayType at = (ArrayType) t;
			resolveQualifiedName(at.getComponentType(), sb);
			sb.append("[]");
			return true;
		} else if(t.isParameterizedType()){
			ParameterizedType pt = (ParameterizedType)t;
			resolveQualifiedName(pt.getType(),sb);
			sb.append("<");
			boolean comma = false;
			for( Type typeArg:(List<Type>)pt.typeArguments()){
				if( comma){
					sb.append(',');
				}
				comma = true;
				if(!tryResolveQualifiedName(typeArg, sb) ){
					sb.append(typeArg);
				}
			}
			sb.append(">");
			return true;
		}
		return false;
	}
	
	public static String resolveQualifiedName(AbstractTypeDeclaration type) {
		if( type.resolveBinding() != null){
			return compiledNameToSourceName(type.resolveBinding().getQualifiedName());
		}
		//TODO:handle anonymous inner classes....
		List<String> parts = newArrayListWithCapacity(5);
		//just adds the simple name
		parts.add(type.getName().getFullyQualifiedName());
		ASTNode parent = type.getParent();
		while (parent != null) {
			if (parent instanceof AbstractTypeDeclaration) {
				//just adds the enclosing types simple name
				parts.add(((AbstractTypeDeclaration) parent).getName().getFullyQualifiedName());
			}
			parent = parent.getParent();
		}
		String pkg = getPackageFor(type);
		if (pkg != null) {
			parts.add(pkg);
		}
		Collections.reverse(parts);
		return Joiner.on('.').join(parts);
	}
	
	public static String resolveQualifiedNameElseShort(Name name) {
		if (name.isQualifiedName()) {
			return name.getFullyQualifiedName();
		} else {
			String fqdn = resolveQualifiedNameOrNull((SimpleName)name);
			if (fqdn == null) {
				fqdn = ((SimpleName)name).getIdentifier();
			}
			return fqdn;
		}
	}
	
	/**
	 * Extract the fully qualified name from the given name, looking up parent if required
	 * @param name
	 * @return
	 */
	public static String resolveQualifiedName(Name name) {
		String fqdn = resolveQualifiedNameOrNull(name);
		if (fqdn == null) {
			throw new MutateException("Could not resolve simple name '%s' defined in '%s'", name.getFullyQualifiedName(), getCompilationUnit(name));
		}
		return fqdn;
	}
	
	private static String resolveQualifiedNameOrNull(Name name) {
		if (name.isQualifiedName()) {
			return compiledNameToSourceName(name.getFullyQualifiedName());
		} else {
			return resolveQualifiedNameOrNull((SimpleName)name);
		}
	}
	

	@VisibleForTesting
	static String resolveQualifiedNameOrNull(SimpleName name) {
		if(name.resolveTypeBinding() != null){
			return compiledNameToSourceName(name.resolveTypeBinding().getQualifiedName());
		}
		CompilationUnit cu = getCompilationUnit(name);
		String fqdn = resolveQualifiedNameFromDeclaredTypesOrNull(name, cu);
		if (fqdn == null) {
			fqdn = resolveQualifiedNameFromImportsOrNull(name, cu);
		}
		
		if( fqdn == null ){
			fqdn = resolveQualifiedNameFromClassLoaderOrNull(name);
		}
		//TODO:look in all parent type, interfaces for a type declared as such
		//...
		
		return fqdn;
	}

	private static CompilationUnit getCompilationUnit(ASTNode node) {
		ASTNode root = node.getRoot();
		if (root instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) root;
			return cu;
		}
		throw new MutateException("Can't find compilation unit node");
	}
	
	@VisibleForTesting
	static String resolveQualifiedNameFromImportsOrNull(SimpleName name, CompilationUnit cu) {
		// not a locally declared type, look through imports
		String nameWithDot = "." + name.getIdentifier();
		List<ImportDeclaration> imports = cu.imports();
		for (ImportDeclaration imprt : imports) {
			String fqn = imprt.getName().getFullyQualifiedName();
			if (fqn.equals(name)) {
				return fqn;
			} else if (fqn.endsWith(nameWithDot)) {
				return fqn;
			}
		}
		//look for wildcards (star imports)
		ClassLoader cl = ClassUtil.getClassLoaderForResolving();
		for (ImportDeclaration imprt : imports) {
			if( imprt.isOnDemand()) {//aka foo.bar.*
				String pkgName = imprt.getName().getFullyQualifiedName();
				String className = pkgName + nameWithDot;
				if(ClassUtil.canLoadClass(cl, className)){
					return className;
				}
			}
		}
		
		return null;
	}
	
	private static String resolveQualifiedNameFromClassLoaderOrNull(SimpleName name) {
		String pkg = getPackagePrefixFrom(getCompilationUnit(name));
		return resolveQualifiedNameFromClassLoaderOrNull(name, pkg, "java.lang.");
	}
	
	private static String resolveQualifiedNameFromClassLoaderOrNull(SimpleName name, String... packagePrefixes) {
		ClassLoader cl = ClassUtil.getClassLoaderForResolving();
		for (String prefix : packagePrefixes) {
			prefix = prefix == null ? "" : prefix;
			String className = prefix + name.getIdentifier();
			if(ClassUtil.canLoadClass(cl, className)){
				return className;
			}
		}
		return null;
	}

	/**
	 * Attempt to resolve the given name from all the declared types in the given compilation unit.
	 *
	 *
	 * <p>E.g, given
	 * <pre>
	 * package com.mycompany;
	 * 
	 * public class Foo {
	 *   public class Foo2 {
	 *   	public class Foo3 {
	 *     		Foo2 doIt(){return new Bar());
	 *   	}
	 *   }
	 *   
	 *   public class Bar extends Foo2 {}
	 * }
	 * 
	 * </pre>
	 * when passing in the name of the return type for the do it method, this will return 'com.mycompany.Foo.Foo2'. For the instantiated type this
	 * would be 'com.mycompany.Foo.Bar'
	 * </p>
	 * @param name
	 * @param cu
	 * @return
	 */
	@VisibleForTesting
	static String resolveQualifiedNameFromDeclaredTypesOrNull(SimpleName name, CompilationUnit cu) {
		TypeDeclaration parentType = getEnclosingTypeOrNull(name);
		return resolveQualifiedNameFromDeclaredTypeOrNull(name,parentType);
	}

	/**
	 * Resolve the given name by looking in the given type for any matching embedded 
	 * classes/enums/interfaces/annotations. If none found walk up the ast node
	 * till we hit the compilation unit, doing the same along the way
	 * 
	 * @param name
	 * @param type
	 * @return the qualified name or null if it could not be resolved
	 */
	@VisibleForTesting
	static String resolveQualifiedNameFromDeclaredTypeOrNull(SimpleName name, TypeDeclaration type) {
		if( type == null ){
			return null;
		}
		String nameIdentifier = name.getIdentifier();
		//declared class types
		String fqdn = resolveQualifiedNameFromChildTypesOrNull(name, type);
		if( fqdn != null){
			return fqdn;
		}
		//TODO:enums -does this work?
		//TODO:interfaces
		//TODO: need the '$' in the name as in com.foo.bar.OuterClass$InnerClass. Is this consistent? the dollar bit?
		//can we rely on it?
		List<BodyDeclaration> bodies = type.bodyDeclarations();
		for( BodyDeclaration body:bodies){
			if( body instanceof EnumDeclaration){
				EnumDeclaration enumDecl = (EnumDeclaration)body;
				if( nameMatches( nameIdentifier, enumDecl.getName())){
					return packageAndName(enumDecl, nameIdentifier);
				}
			}
			if( body instanceof AnnotationTypeDeclaration ){
				AnnotationTypeDeclaration anonDec = (AnnotationTypeDeclaration)body;
				if( nameMatches( nameIdentifier, anonDec.getName())){
					return packageAndName(anonDec, nameIdentifier);
				}
			}
//			if( body instanceof Interface) {
//			
//			}
			
		}
		//TODO:annotations
		
		//not found yet, lets now try parents
		TypeDeclaration parentType = getEnclosingTypeOrNull(type);
		return resolveQualifiedNameFromDeclaredTypeOrNull(name, parentType);
	}
	
	private static boolean nameMatches(String haveName, SimpleName name){
		return haveName.matches(name.getIdentifier() );
	}
	
	private static String packageAndName(ASTNode node, String name){
		CompilationUnit cu = getCompilationUnit(node);
		String pkg = getPackagePrefixFrom(cu);
		TypeDeclaration parent = getEnclosingTypeOrNull(node);		
		while (parent != null) {
			pkg = pkg + parent.getName().getIdentifier() + ".";
			parent = getEnclosingTypeOrNull(parent);
		}
		return pkg + name;
	}
	
	private static String resolveQualifiedNameFromChildTypesOrNull(SimpleName name, TypeDeclaration type) {
	    TypeDeclaration[] childTypes = type.getTypes();//TODO:get interfaces and enums and stuff too. Filter body decl
		for(AbstractTypeDeclaration childType:childTypes){
			if( name.getIdentifier().matches(childType.getName().getIdentifier())){
				return resolveQualifiedName(type) + "." + name;
			}
		}
		return null;
    }
	
	/**
	 * Return the package prefix ending with a full stop if a package has been declared, or empty
	 * if no package has been declared
	 * 
	 * @param node
	 */
	private static String getPackagePrefixFrom(ASTNode node) {
		String pkg = getPackageFor(node);
		return pkg == null ? "" : pkg + ".";
	}
	
	public static String getPackageFor(ASTNode node){
		CompilationUnit cu = getCompilationUnit(node);
		String pkg = null;
		if (cu.getPackage() != null) {
			pkg = cu.getPackage().getName().getFullyQualifiedName();
		}
		return pkg;
	}
	
	/**
	 * Return the first parent node which is of type {@link TypeDeclaration} or null if no parent could be found
	 */
	private static TypeDeclaration getEnclosingTypeOrNull(ASTNode node) {
		ASTNode parent = node.getParent();
		while (parent != null) {
			if (parent instanceof TypeDeclaration) {
				return (TypeDeclaration) parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

	/**
	 * Compiled names often use '$' for embedded classes. Convert these to dots. Do the same for any other special chars used by compilers
	 * @param klass
	 * @return
	 */
	public static String compiledNameToSourceName(Class<?> klass){
		return compiledNameToSourceName(klass.getName());
	}

	/**
	 * Compiled names often use '$' for embedded classes. Convert these to dots. Do the same for any other special chars used by compilers
	 * @param className
	 * @return
	 */
	public static String compiledNameToSourceName(String className){
		return className.replace('$', '.');
	}
}
