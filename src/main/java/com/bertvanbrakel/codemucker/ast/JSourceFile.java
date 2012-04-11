package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.codemucker.util.SourceUtil;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.bertvanbrakel.test.util.ClassNameUtil;

public class JSourceFile implements JSource, AstNodeProvider {
	
	private final ClassPathResource resource;
	private final AstCreator astCreator;
	//created on demand
	private transient CompilationUnit compilationUnit;

	public JSourceFile(ClassPathResource location, AstCreator astCreator) {
		this.resource = checkNotNull("location", location);
		this.astCreator = checkNotNull("astCreator", astCreator);
	}
	
	@Override
	public ASTNode getAstNode(){
		return compilationUnit;
	}
	

	public void visit(JSourceFileVisitor visitor) {
		if (visitor.visit(this)) {
			CompilationUnit cu = getCompilationUnit();
			if (visitor.visit(cu)) {
				cu.accept(visitor);
				visitor.endVisit(cu);
			}
			visitor.endVisit(this);
		}
	}

	public JSourceFileMutator asMutator(){
		return new JSourceFileMutator(this);
	}
	
	@Override
	public AstCreator getAstCreator() {
		return astCreator;
	}

	@Override
	public ClassPathResource getLocation(){
		return resource;
	}
	
	/**
	 * @deprecated use {@link #getMainType()}
	 */
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
		String pkg = resource.getPackagePart();
		if( pkg != null ){
			return pkg + "." + simpleName;
		}
		return simpleName;
	}
	
	public String getSimpleClassnameBasedOnPath(){
		String name = ClassNameUtil.upperFirstChar(resource.getBaseFileNamePart());
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
		throw new CodemuckerException("Can't find type named %s in %s. Found %s", simpleName, resource.getRelPath(), Arrays.toString(names.toArray()));
	}
	

	public JType getTypeWithName(Class<?> type) {
		return getTypeWithName(type.getSimpleName());
	}
	
	/**
	 * Look through all top level types and all their children for any type with the given name. 
	 */
	public JType getTypeWithName(final String simpleName) {
		Matcher<JType> matcher = new Matcher<JType>() {
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
		throw new CodemuckerException("Could not find type with name '%s' in %s", simpleName, this);
	}

	public List<JType> findAllTypes(){
		return internalFindTypesMatching(JType.MATCH_ALL_TYPES);
	}
	
	public List<JType> findTypesMatching(Matcher<JType> matcher){
		return internalFindTypesMatching(matcher);
	}
	
	private List<JType> internalFindTypesMatching(Matcher<JType> matcher){
		List<JType> found = newArrayList();
		for( JType type:getJTypes()){
			if( matcher.matches(type)){
				found.add(type);
			}
			type.findChildTypesMatching(matcher, found);
		}
		return found;
	}

	private static Collection<String> extractNames(List<AbstractTypeDeclaration> types){
		Collection<String> names = newArrayList();
		for( AbstractTypeDeclaration type:types){
			names.add(type.getName().toString());
		}
		return names;
	}

	public List<JType> getJTypes() {
		List<JType> javaTypes = newArrayList();
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
			compilationUnit = astCreator.create(resource.getFile());
		}
		return compilationUnit;
	}

	public String readSource() {
		return SourceUtil.readSource(resource.getFile());
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}