package com.bertvanbrakel.codemucker.util;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.util.Collections;
import java.util.List;

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

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

public class JavaNameUtil {

	public static String getQualifiedName(Type t){
		StringBuilder sb = new StringBuilder();
		resolveQualifiedNameFor(t, sb);
		return sb.toString();
	}
	
	public static void resolveQualifiedNameFor(Type t, StringBuilder sb){
		if (t.isPrimitiveType()) {
			sb.append(((PrimitiveType) t).getPrimitiveTypeCode().toString());
		} else if (t.isSimpleType()) {
			SimpleType st = (SimpleType) t;
			sb.append(JavaNameUtil.getQualifiedName(st.getName()));
		} else if (t.isQualifiedType()) {
			QualifiedType qt = (QualifiedType) t;
			sb.append(JavaNameUtil.getQualifiedName(qt.getName()));
		} else if (t.isArrayType()) {
			ArrayType at = (ArrayType) t;
			resolveQualifiedNameFor(at.getComponentType(), sb);
			sb.append("[]");
		} else if( t.isParameterizedType()){
			ParameterizedType pt = (ParameterizedType)t;
			resolveQualifiedNameFor(pt.getType(),sb);
			sb.append("<");
			boolean comma = false;
			for( Type typeArg:(List<Type>)pt.typeArguments()){
				if( comma){
					sb.append(',');
				}
				comma = true;
				resolveQualifiedNameFor(typeArg, sb);
			}
			sb.append(">");
		} else {
			throw new CodemuckerException("Currently don't know how to handle type:" + t);
		}
	}
	
	public static String getQualifiedNameFor(AbstractTypeDeclaration type) {
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
	/**
	 * Extract teh fuly qualified name from the given name, looking up parent if required
	 * @param name
	 * @return
	 */
	public static String getQualifiedName(Name name) {
		if (name.isQualifiedName()) {
			return name.getFullyQualifiedName();
		} else {
			return resolveFqn((SimpleName)name);
		}
	}

	/* package for testing */ static String resolveFqn(SimpleName name) {
		CompilationUnit cu = getCompilationUnit(name);
		String fqdn = resolveFqnFromDeclaredTypes(cu, name);
		if (fqdn == null) {
			fqdn = resolveFqnFromImports(cu, name);
		}
		if( fqdn == null ){
			fqdn = resolveFqdnFromClassLoader(name);
		}
		if (fqdn == null) {
			throw new CodemuckerException("Could not resolve simple name '%s' defined in '%s'", name.getFullyQualifiedName(), getCompilationUnit(name));
		}
		return fqdn;
	}

	private static CompilationUnit getCompilationUnit(ASTNode node) {
		ASTNode root = node.getRoot();
		if (root instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) root;
			return cu;
		}
		throw new CodemuckerException("Can't find compilation unit node");
	}
	
	/* package for testing */ static String resolveFqnFromImports(CompilationUnit cu, SimpleName name) {
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
		return null;
	}
	
	static String resolveFqdnFromClassLoader(SimpleName name) {
		String pkg = getPackagePrefixFrom(getCompilationUnit(name));
		return resolveFqdnFromClassLoader(name, pkg, "java.lang.");
	}
	
	static String resolveFqdnFromClassLoader(SimpleName name, String... packagePrefixes) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		for (String prefix : packagePrefixes) {
			try {
				prefix = prefix == null ? "" : prefix;
				Class<?> type = cl.loadClass(prefix + name.getIdentifier());
				return type.getName();
			} catch (ClassNotFoundException e) {
				// do nothing. Just try next prefix
			} catch (NoClassDefFoundError e) {
				// do nothing. Just try next prefix
				System.out.println("bad");
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
	 *     		Foo2 doIt(){return null);
	 *   	}
	 *   } 
	 * }
	 * </pre>
	 * when passing in the name of the return type for the do it method, this will return com.mycompany.Foo2
	 * </p>
	 * @param cu
	 * @param name
	 * @return
	 */
	@VisibleForTesting
	static String resolveFqnFromDeclaredTypes(CompilationUnit cu, SimpleName name) {
		TypeDeclaration parentType = getEnclosingTypeOrNull(name);
		return resolveFqnFromDeclaredType(parentType,name);
	}

	@VisibleForTesting	
	static String resolveFqnFromDeclaredType(TypeDeclaration type, SimpleName name) {
		if( type == null ){
			return null;
		}
		String nameIdentifier = name.getIdentifier();
		//declared class types
		String fqdn = resolveFqdnFromChildTypes(type, name);
		if( fqdn != null){
			return fqdn;
		}
		//TODO:enums -does this work?
		//TODO:interfaces
		//TODO: need the '$' int he name as in com.foo.bar.OuterCLass$InnerClass. Is this consistent? the dollar bit?
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
		return resolveFqnFromDeclaredType(parentType, name);
	}
	
	private static boolean nameMatches(String haveName, SimpleName name){
		return haveName.matches(name.getIdentifier() );
	}
	
	private static String packageAndName(ASTNode node, String name){
		CompilationUnit cu = getCompilationUnit(node);
		String pkg = getPackagePrefixFrom(cu);
		TypeDeclaration parent = getEnclosingTypeOrNull(node);		
		while (parent != null) {
			pkg = pkg + parent.getName().getIdentifier() + "$";
			parent = getEnclosingTypeOrNull(parent);
		}
		return pkg + name;
	}
	
	private static String resolveFqdnFromChildTypes(TypeDeclaration type, SimpleName name) {
	    TypeDeclaration[] childTypes = type.getTypes();//TODO:get interfaces and enums and stuff too. Filter body decl
		for(AbstractTypeDeclaration childType:childTypes){
			if( name.getIdentifier().matches(childType.getName().getIdentifier())){
				return getPackagePrefixFrom(type) + name;
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

}
