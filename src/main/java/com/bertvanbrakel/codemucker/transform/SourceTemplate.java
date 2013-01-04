package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JAstParser;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.DirectoryRoot;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.util.ProjectFinder;
import com.google.inject.Inject;

/**
 * Template which exposes various parse methods to convert the template text into various Java ast nodes
 */
@NotThreadSafe
public class SourceTemplate extends AbstractTemplate<SourceTemplate>
{
	@Inject
	private final JAstParser parser;
	
	/**
	 * Used by the DI container to set the default
	 * @param parser
	 */
	@Inject
	public SourceTemplate(JAstParser parser){
		this.parser = checkNotNull(parser,"expect parser");
	}
	
	/**
	 * Try to parse the template text as an expression
	 * @return
	 */
	public Expression asExpressionNode() {
		return (Expression) parser.parseNode(interpolate(),ASTParser.K_EXPRESSION);
	}

	/**
	 * Try to parse the template text as a field and wrap as a JField
	 * @return
	 */
	public JField asJField(){
		return new JField(asFieldNode());
	}
	
	/**
	 * Try to parse the template text as a field
	 * @return
	 */
	public FieldDeclaration asFieldNode(){
		TypeDeclaration type = toTempWrappingType();
		assertEquals("expected a single field", 1, type.getFields().length);
		FieldDeclaration fieldNode = type.getFields()[0];
		return fieldNode;
	}

	/**
	 * Parse the current template as a constructor checking to ensure this is a syntactically valid constructor.
	 * @return
	 */
	public MethodDeclaration asConstructorNode(){
		//TODO:decide and sort out exception types to throw. Assertions or custom bean assertions?
		TypeDeclaration type = toTempWrappingType();
		assertEquals("Expected a single constructor", 1, type.getMethods().length);
		MethodDeclaration method = type.getMethods()[0];
		if (method.getReturnType2() != null) {
			throw new CodemuckerException("Constructors should not have any return type. Constructor was %s",method);
		}
		method.setConstructor(true);
		return method;
	}
	
	/**
	 * Try to parse the template text as a method and wrap as a JMethod
	 * 
	 * @return
	 */
	public JMethod asJMethod(){
		return new JMethod(asMethodNode());
	}
	
	/**
	 * Try to parse the template text as a method
	 * @return
	 */
	public MethodDeclaration asMethodNode(){
		TypeDeclaration type = toTempWrappingType();
		assertEquals("Expected a single method", 1, type.getMethods().length);
		MethodDeclaration method = type.getMethods()[0];
		return method;
	}
	
	/**
	 * Parse the current template as a type and wrap as a JType
	 * @return
	 */
	public JType asJType(){
		return JType.from(asTypeNode());
	}
	
	/**
	 * Parse the current template as a type
	 * @return
	 */
	public AbstractTypeDeclaration asTypeNode() {
		CompilationUnit cu = asCompilationUnit();
		if( cu.types().size() == 1){
			return (AbstractTypeDeclaration) cu.types().get(0);
		}
		
		if( cu.types().size() == 0){
			throw new CodemuckerException("Source template does not contain any types. Expected 1 but got 0. Parsed source %s",interpolate());
		}
		throw new CodemuckerException("Source template contains more than one type. Expected 1 but got %d. Parsed source %s",cu.types().size(), interpolate());
	}

	/**
	 * Parse the current template as a compilation unit and use the package declarations in the current template, or 
	 * the default package if there is none, to generate the 
	 * base part of the full class name. Use the given simple name to  determine the name of the file within the package.
	 * 
	 * @param simpleName the file name. E.g. MyClass
	 * @return
	 */
	public JSourceFile asSourceFileWithSimpleName(String simpleName) {
		checkLegalIdentifier(simpleName);

		CharSequence src = interpolate();
		CompilationUnit cu = parser.parseCompilationUnit(src);
		String fqn = simpleNameToFullNameIn(simpleName, cu);
		ClassPathResource resource = newTmpResourceWithPath(fqnToRelPath(fqn));
		
		return new JSourceFile(resource, cu, src);
	}

	private static String simpleNameToFullNameIn(String simpleName, CompilationUnit cu) {
	    PackageDeclaration pkg = cu.getPackage();
		if( pkg != null){
			return pkg.getName().getFullyQualifiedName() + "." + simpleName;
		} else {
			return simpleName;
		}
    }
	
	/**
	 * Try to parse the template text as a source file trying to automatically detect the source path
	 * based on the first top level type. A temporary
	 * root will be used.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSourceFile asSourceFile() {	
		CharSequence src = interpolate();
		CompilationUnit cu = parser.parseCompilationUnit(src);
		List<AbstractTypeDeclaration> types = cu.types();
		
		JType mainType;
		if(types.isEmpty()){
			throw new CodemuckerException("no types found in compilation unit so couldn't determine source path to generate");
		} else if(types.size() == 1){
			mainType = JType.from(types.get(0));
		} else {
			for(AbstractTypeDeclaration node:types){
				JType type = JType.from(node);
				if( type.getModifiers().isPublic()){
					mainType = type;
				}
			}
			throw new CodemuckerException("Multiple top level types in compilation unit and could not determine one to use. Try setting one to public access");
		}
		
		String fullName = mainType.getFullName();
		ClassPathResource resource = newTmpResourceWithPath(fqnToRelPath(fullName));
		return new JSourceFile(resource, cu, src);
	}
	
	/**
	 * Try to parse the template text as a source file with the given relative source path. A temporary
	 * root will be used.
	 * 
	 * @param fqn the fully qualified source name without the file extension. As in 'foo.bar.MyClass'.
	 * @return
	 */
	public JSourceFile asSourceFileWithFullName(String fqn) {
		return asSourceFileWithPath(fqnToRelPath(fqn));
	}
	
	private static String fqnToRelPath(String fqn){
		return  fqn.replace('.','/');
	}

	/**
	 * Try to parse the template text as a compilation unit and save to the given relative path as a 
	 * java source file. A temporary resource root directory is used.
	 * 
	 * @param relPath
	 * @return
	 */
	public JSourceFile asSourceFileWithPath(String relPath) {
		relPath = checkRelPath(relPath) + ".java";
		
		CharSequence src = interpolate();
		CompilationUnit cu = parser.parseCompilationUnit(src);
		ClassPathResource resource = newTmpResourceWithPath(relPath);
		return new JSourceFile(resource, cu, src);
	}

	private ClassPathResource newTmpResourceWithPath(String relPath) {
	    Root root = newTmpRoot();
		ClassPathResource resource = new ClassPathResource(root, relPath);
	    return resource;
    }

	private String checkRelPath(String relPath){
		checkNotNull(StringUtils.trimToNull(relPath), "expect non null or empty relative path");

		char c;
		for (int i = 0; i < relPath.length(); i++) {
			c = relPath.charAt(i);
			if (!(Character.isJavaIdentifierPart(c) || c == '/')) {
				throw new IllegalArgumentException(
						String.format("Relative paths can only contain valid java identifiers and forward slashes. Instead found '%s', for %s", c, relPath));
			}
		}
		return relPath;
	}
	
	private void checkLegalIdentifier(String name){
		checkNotNull(StringUtils.trimToNull(name), "expect non null or empty name");
		
		char c;
		for (int i = 0; i < name.length(); i++) {
			c = name.charAt(i);
			if (!(Character.isJavaIdentifierPart(c))) {
				throw new IllegalArgumentException(
						String.format("Names can contain only valid java identifiers. Instead found '%s', for %s", c, name));
			}
		}
	}
	
	private static Root newTmpRoot(){
		File dir = ProjectFinder.getDefaultResolver().getTmpDir();
		File tmpDir = new File(dir,UUID.randomUUID().toString() + "/");
		tmpDir.mkdirs();
		
		return new DirectoryRoot(tmpDir,Root.RootType.GENERATED_SRC);
	}

	private TypeDeclaration toTempWrappingType() {
		String tmpTypeName = SourceTemplate.class.getSimpleName() + "__TmpCodeMuckerWrapperKlass__";
		String src = "class " + tmpTypeName + "{" + interpolate()  + "}";
		CompilationUnit cu = parser.parseCompilationUnit(src);
		List<?> types = cu.types();
		assertEquals("expected only a single type", 1, types.size());
		TypeDeclaration type = (TypeDeclaration) types.get(0);
		return type;
	}

	/**
	 * Try to parse the template text as a compilation unit
	 * 
	 * @return
	 */
	public CompilationUnit asCompilationUnit() {
		return parser.parseCompilationUnit(interpolate());
	}
}