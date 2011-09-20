package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.codemucker.ast.finder.ClasspathResource;
import com.bertvanbrakel.codemucker.ast.finder.JavaTypeMatcher;
import com.bertvanbrakel.codemucker.util.SourceUtil;
import com.bertvanbrakel.test.util.ClassNameUtil;

public class JavaSourceFile {
	
	private final ClasspathResource location;
	private final AstCreator astCreator;
	//created on demand
	private transient CompilationUnit compilationUnit;

	public JavaSourceFile(ClasspathResource location, AstCreator astCreator) {
		checkNotNull("location", location);
		checkNotNull("astCreator", astCreator);
		this.location = location;
		this.astCreator = astCreator;
	}

	public void visit(JavaSourceFileVisitor visitor) {
		if (visitor.visit(this)) {
			CompilationUnit cu = getCompilationUnit();
			if (visitor.visit(cu)) {
				cu.accept(visitor);
				visitor.endVisit(cu);
			}
			visitor.endVisit(this);
		}
	}

	public JavaSourceFileMutator asMutator(){
		return new JavaSourceFileMutator(this);
	}
	
	public AstCreator getAstCreator() {
		return astCreator;
	}

	public ClasspathResource getLocation(){
		return location;
	}
	
	public AbstractTypeDeclaration getMainType() {
		String simpleName = getSimpleClassnameBasedOnPath();
		return getTopTypeWithName(simpleName);
	}
	
	public String getClassnameBasedOnPath(){
		String simpleName = getSimpleClassnameBasedOnPath();
		String pkg = location.getPackagePart();
		if( pkg != null ){
			return pkg + "." + simpleName;
		}
		return simpleName;
	}
	
	public String getSimpleClassnameBasedOnPath(){
		String name = ClassNameUtil.upperFirstChar(location.getFilenamePart());
		return name;
	}
	
	/**
	 * Look through just the top level types for this file for a type with the given name
	 */
	public AbstractTypeDeclaration getTopTypeWithName(String simpleName){
		List<AbstractTypeDeclaration> types = getTypes();
		for( AbstractTypeDeclaration type:types){
			if( simpleName.equals(type.getName().toString())){
				return type;
			}
		}
		Collection<String> names = extractNames(types);
		throw new CodemuckerException("Can't find type named %s in %s. Found %s", simpleName, location.getRelativePath(), Arrays.toString(names.toArray()));
	}
	
	/**
	 * Look through all top level types and all their children for any type with the given name. 
	 */
	public List<JavaType> findTypesWithName(final String simpleName){
		JavaTypeMatcher matcher = new JavaTypeMatcher() {
			@Override
			public boolean matchType(JavaType found) {
				return found.getSimpleName().equals(simpleName);
			}
		};
		return internalFindTypesMatching(matcher);
	}
	
	public List<JavaType> findTypesMatching(JavaTypeMatcher matcher){
		return internalFindTypesMatching(matcher);
	}
	
	private List<JavaType> internalFindTypesMatching(JavaTypeMatcher matcher){
		List<JavaType> found = new ArrayList<JavaType>();
		for( JavaType type:getJavaTypes()){
			if( matcher.matchType(type)){
				found.add(type);
			}
			type.findChildTypesMatching(matcher);
		}
		return found;
	}

	private static Collection<String> extractNames(List<AbstractTypeDeclaration> types){
		Collection<String> names = new ArrayList<String>();
		for( AbstractTypeDeclaration type:types){
			names.add(type.getName().toString());
		}
		return names;
	}

	public List<JavaType> getJavaTypes() {
		List<JavaType> javaTypes = new ArrayList<JavaType>();
		for( AbstractTypeDeclaration type:getTypes()){
			javaTypes.add(new JavaType(this, type));
		}
		return javaTypes;
	}
	
	@SuppressWarnings("unchecked")
	public List<AbstractTypeDeclaration> getTypes() {
		return getCompilationUnit().types();
	}
	
	public CompilationUnit getCompilationUnit() {
		if (compilationUnit == null) {
			compilationUnit = astCreator.create(location.getFile());
		}
		return compilationUnit;
	}

	public String readSource() {
		return SourceUtil.readSource(location.getFile());
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}