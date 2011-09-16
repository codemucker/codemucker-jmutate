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
import com.bertvanbrakel.codemucker.util.SourceUtil;
import com.bertvanbrakel.test.bean.ClassUtils;

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

	public AstCreator getAstCreator() {
		return astCreator;
	}

	public ClasspathResource getLocation(){
		return location;
	}
	
	public AbstractTypeDeclaration getMainType() {
		String simpleName = getSimpleClassnameBasedOnPath();
		return getTypeWithName(simpleName);
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
		String name = ClassUtils.upperFirstChar(location.getFilenamePart());
		return name;
	}
	
	public AbstractTypeDeclaration getTypeWithName(String simpleName){
		List<AbstractTypeDeclaration> types = getTypes();
		//String className = srcFile.getSimpleClassnameBasedOnPath();
		for( AbstractTypeDeclaration type:types){
			if( simpleName.equals(type.getName().toString())){
				return type;
			}
		}
		Collection<String> names = extractNames(types);
		throw new CodemuckerException("Can't find type named %s in %s. Found %s", simpleName, location.getRelativePath(), Arrays.toString(names.toArray()));
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