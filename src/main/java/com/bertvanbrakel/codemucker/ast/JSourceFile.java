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
import com.bertvanbrakel.codemucker.ast.finder.matcher.JTypeMatcher;
import com.bertvanbrakel.codemucker.util.SourceUtil;
import com.bertvanbrakel.test.util.ClassNameUtil;

public class JavaSourceFile implements JSource {
	
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
	
	@Override
	public AstCreator getAstCreator() {
		return astCreator;
	}

	@Override
	public ClasspathResource getLocation(){
		return location;
	}
	
	@Deprecated
	public JType getMainJType() {
		return getMainType();
	}

	public JType getMainType() {
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
	
	public JType getTopTypeWithName(Class<?> type){
		return getTopTypeWithName(type.getSimpleName());
	}
	/**
	 * Look through just the top level types for this file for a type with the given name
	 */
	public JType  getTopTypeWithName(String simpleName){
		List<AbstractTypeDeclaration> types = getTypes();
		for( AbstractTypeDeclaration type:types){
			if( simpleName.equals(type.getName().toString())){
				return new JType(type);
			}
		}
		Collection<String> names = extractNames(types);
		throw new CodemuckerException("Can't find type named %s in %s. Found %s", simpleName, location.getRelativePath(), Arrays.toString(names.toArray()));
	}
	

	public JType getTypeWithName(Class<?> type) {
		return getTypeWithName(type.getSimpleName());
	}
	
	/**
	 * Look through all top level types and all their children for any type with the given name. 
	 */
	public JType getTypeWithName(final String simpleName) {
		JTypeMatcher matcher = new JTypeMatcher() {
			@Override
			public boolean matches(JType found) {
				return found.getSimpleName().equals(simpleName);
			}
		};
		List<JType> found = internalFindTypesMatching(matcher);
		if (found.size() > 1) {
			throw new CodemuckerException("Invalid source file, found more than one type with name '%s'", simpleName);
		}
		if (found.size() == 1) {
			return found.get(0);
		}
		return null;
	}

	public List<JType> findAllTypes(){
		return internalFindTypesMatching(JType.MATCH_ALL_TYPES);
	}
	
	public List<JType> findTypesMatching(JTypeMatcher matcher){
		return internalFindTypesMatching(matcher);
	}
	
	private List<JType> internalFindTypesMatching(JTypeMatcher matcher){
		List<JType> found = new ArrayList<JType>();
		for( JType type:getJavaTypes()){
			if( matcher.matches(type)){
				found.add(type);
			}
			type.findChildTypesMatching(matcher, found);
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

	public List<JType> getJavaTypes() {
		List<JType> javaTypes = new ArrayList<JType>();
		for( AbstractTypeDeclaration type:getTypes()){
			javaTypes.add(new JType(type));
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