package org.codemucker.jmutate;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmatch.Assert;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JCompilationUnit;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.annotation.NotThreadSafe;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.inject.Inject;


/**
 * Template which exposes various parse methods to convert the template text into various Java ast nodes
 */
@NotThreadSafe
public class SourceTemplate extends AbstractTemplate<SourceTemplate>
{
    private static final String NL = System.getProperty("line.separator");
    
	private final JAstParser parser;
	
	private final Root snippetRoot;
    
	/**
	 * Used by the DI container to set the default
	 * @param parser
	 */
	@Inject
	public SourceTemplate(JAstParser parser, Root snippetRoot){
		this.parser = checkNotNull(parser,"expect parser");
		this.snippetRoot = checkNotNull(snippetRoot, "expect snippet root");
	}
	
	/**
	 * Create a child template copying all this templates settings except for the actual text. Changes in the parent
	 * template are not reflected in the child (or vice-versa)
	 * @return
	 */
	public SourceTemplate child(){
		SourceTemplate child = new SourceTemplate(parser,snippetRoot);
		child.setVars(cloneVars());
		return child;
	}
	
	public SourceTemplate add(SourceTemplate child) {
		p(child.interpolateTemplate());
		return this;
	}
	
	/**
	 * @see {@link #setVar(String, Class)}}
	 * 
	 * @param name
	 * @param klass
	 * @return
	 */
	public SourceTemplate v(String name,Class<?> klass){
		setVar(name,klass);
		return this;
	}
	
	/**
	 * Set the class name as a var. Replaces any $ in the class name with a '.' to handle the difference
	 * between what the compiler outputs and what it expects as input
	 * @param name
	 * @param klass
	 * @return
	 */
	public SourceTemplate setVar(String name,Class<?> klass){
		setVar(name,NameUtil.compiledNameToSourceName(klass));
		return this;
	}

	
	/**
	 * Print the class name replacing any $ in the class name with a '.' to handle the difference
	 * between what the compiler outputs and what it expects as input
	 * @param klass
	 * @return
	 */
	public SourceTemplate p(Class<?> klass){
		p(NameUtil.compiledNameToSourceName(klass));
		return this;
	}
	
	
	/**
	 * Print the class name replacing any $ in the class name with a '.' to handle the difference
	 * between what the compiler outputs and what it expects as input
	 * @param klass
	 * @return
	 */
	public SourceTemplate pl(Class<?> klass){
		pl(NameUtil.compiledNameToSourceName(klass));
		return this;
	}
	
	
	/**
	 * Print the class name replacing any $ in the class name with a '.' to handle the difference
	 * between what the compiler outputs and what it expects as input
	 * @param klass
	 * @return
	 */
	public SourceTemplate println(Class<?> klass){
		println(NameUtil.compiledNameToSourceName(klass));
		return this;
	}
	
	
	/**
	 * Replace all single quotes in the template with double quotes. This is useful if you need to perform many string assignments in the generated source
	 * and you don't want to have to escape it all
	 * 
	 * This only affects the template body, it does
	 * not affect any of the variables, nor anything the variables resolve to. This just performs a string replace
	 * on the template body on the contents of the body at the time this method is invoked.
	 * 
	 * If you wish to replace everything then you will need to build the template first, then perform a string replace
	 * manually
	 * @return
	 */
	public SourceTemplate singleToDoubleQuotes(){
		replace('\'', '"');
		return this;
	}
	/**
	 * Try to parse the template text as an expression
	 * @return
	 */
	public Expression asExpressionNode() {
		return (Expression) parser.parseNode(interpolateTemplate(),ASTParser.K_EXPRESSION, null);
	}

	/**
	 * Try to parse the template text as a field and wrap as a JField
	 * @return
	 */
	public JField asResolvedJField(){
		return JField.from(asResolvedFieldNode());
	}
	
	/**
	 * Try to parse the template text as a field and wrap as a JField
	 * @return
	 */
	public JField asJFieldSnippet(){
		return JField.from(asFieldNodeSnippet());
	}
	
	/**
	 * Try to parse the template text as a field
	 * @return
	 */
	public FieldDeclaration asResolvedFieldNode(){
		return asFieldNode(true);
	}

	/**
	 * Try to parse the template text as a field
	 * @return
	 */
	public FieldDeclaration asFieldNodeSnippet(){
		return asFieldNode(false);
	}
	
	
	private FieldDeclaration asFieldNode(boolean resolve){
		TypeDeclaration type = toTempWrappingType(false,false);
		Assert.assertEquals("expected a single field", 1, type.getFields().length);
		FieldDeclaration fieldNode = type.getFields()[0];
		return fieldNode;
	}
	
	public JMethod asConstructorSnippet(){
		return JMethod.from(asConstructorNode(false));
	}
	
	/**
	 * Parse the current template as a constructor checking to ensure this is a syntactically valid constructor.
	 * @return
	 */
	public MethodDeclaration asConstructorNodeSnippet(){
		return asConstructorNode(false);
	}
	
	
	public MethodDeclaration asResolvedConstructorNode(){
		return asConstructorNode(true);
	}
	
	private MethodDeclaration asConstructorNode(boolean resolve){
		//TODO:decide and sort out exception types to throw. Assertions or custom bean assertions?
		TypeDeclaration type = toTempWrappingType(resolve,false);
		Assert.assertEquals("Expected a single constructor", 1, type.getMethods().length);
		MethodDeclaration method = type.getMethods()[0];
		if (method.getReturnType2() != null) {
			throw new JMutateException("Constructors should not have any return type. Constructor was %s",method);
		}
		method.setConstructor(true);
		return method;
	}
	
	/**
	 * Try to parse the template text as a method and wrap as a JMethod
	 * 
	 * @return
	 */
	public JMethod asJMethodSnippet(){
		return JMethod.from(asMethodNodeSnippet());
	}
	
	public JMethod asJMethodInterfaceSnippet(){
        return JMethod.from(asMethodNode(false,true));
    }
	/**
	 * Try to parse the template text as a method and wrap as a JMethod
	 * 
	 * @return
	 */
	public JMethod asResolvedJMethod(){
		return JMethod.from(asResolvedMethodNode());
	}
	
	/**
	 * Try to parse the template text as a method
	 * @return
	 */
	public MethodDeclaration asMethodNodeSnippet(){
		return asMethodNode(false,false);
	}
	
	public MethodDeclaration asResolvedMethodNode(){
		return asMethodNode(true,false);
	}
	
	private MethodDeclaration asMethodNode(boolean resolve, boolean isInterface){
		TypeDeclaration type = toTempWrappingType(resolve,isInterface);
		Assert.assertEquals("Expected a single method", 1, type.getMethods().length);
		MethodDeclaration method = type.getMethods()[0];
		return method;
	}
	
	private TypeDeclaration toTempWrappingType(boolean resolve, boolean isInterface) {
		String tmpTypeName = SourceTemplate.class.getSimpleName() + "__TmpWrapperType" + UUID.randomUUID().toString().replace('-', '_') + "__";
		String src = (isInterface?"interface":"class") + " " + tmpTypeName + "{" + NL + interpolateTemplate()  + NL +  "}";
        //Disable resolving as we are making snippets to merge...
		RootResource resource = resolve?fqnToResource(tmpTypeName):null;
		CompilationUnit cu = parser.parseCompilationUnit(src,resource);
		List<?> types = cu.types();
		Assert.assertEquals("expected only a single type", 1, types.size());
		TypeDeclaration type = (TypeDeclaration) types.get(0);
		return type;
	}
	
	public JType asJTypeSnippet(){
		return JType.from(asTypeNodeSnippet());
	}
	
	/**
	 * Parse the current template as a type and wrap as a JType
	 * @return
	 */
	public JType asResolvedJTypeNamed(String fqn){
		return JType.from(asResolvedTypeNodeNamed(fqn));
	}
	
	/**
	 * Parse the current template as a type
	 * @return
	 */
	public AbstractTypeDeclaration asResolvedTypeNodeNamed(String fqn) {
		return asTypeNode(asResolvedCompilationUnitNamed(fqn));
	}
	
	public AbstractTypeDeclaration asTypeNodeSnippet() {
		return asTypeNode(asCompilationUnitSnippet());
	}
	
	private AbstractTypeDeclaration asTypeNode(CompilationUnit cu) {
		if(cu.types().size() == 1){
			return (AbstractTypeDeclaration) cu.types().get(0);
		}
		if(cu.types().size() == 0){
			throw new JMutateException("Source template does not contain any types. Expected 1 but got 0. Parsed source %s",interpolateTemplate());
		}
		throw new JMutateException("Source template contains more than one type. Expected 1 but got %d. Parsed source %s",cu.types().size(), interpolateTemplate());
	}

	/**
	 * Try to parse the template text as a source file with the given relative source path. Uses the current root
	 * 
	 * @param fqn the fully qualified source name without the file extension. As in 'foo.bar.MyClass'.
	 * @return
	 */
	public JSourceFile asResolvedSourceFileNamed(String fqn) {
		checkNotNull(fqn);
		
		RootResource resource = fqnToResource(fqn);
		CharSequence src = interpolateTemplate();
		
		return JSourceFile.fromSource(resource, src, parser);
	}
	
	public JSourceFile asSourceFileSnippet() {
		return asSourceFileSnippet(snippetRoot);
	}

	public JSourceFile asSourceFileSnippet(Root root) {
        CharSequence src = interpolateTemplate();
        CompilationUnit cu = parser.parseCompilationUnit(src, null);//don't get compiler to resolve
        JType mainType = JCompilationUnit.from(cu).findMainType();
        String fqn = mainType.getFullName();
        RootResource resource = fqnToResource(root,fqn);
        return JSourceFile.fromSource(resource, src, cu);
    }
	/**
	 * Try to parse the template text as a compilation unit
	 * 
	 * @return
	 */
	public CompilationUnit asResolvedCompilationUnitNamed(String fqn) {
		return parser.parseCompilationUnit(interpolateTemplate(),fqnToResource(fqn));
	}

	public CompilationUnit asCompilationUnitSnippet() {
		return parser.parseCompilationUnit(interpolateTemplate(),null);
	}
	
	private RootResource fqnToResource(String fqn){
        return fqnToResource(snippetRoot, fqn);
    }
	
	private RootResource fqnToResource(Root root,String fqn){
	    return fqnToResource(root, fqn,JCompiler.JAVA_SRC_EXTENSION);
	}
	
	private RootResource fqnToResource(Root root, String fqn, String extension){
		RootResource resource = null;
		if( fqn != null){
			String path  = expandFqnToRelativePath(fqn);
			resource = new RootResource(root, path + "." + extension);
		}
		return resource;
	}
	
	private String expandFqnToRelativePath(String fqn){
		String expandedFqn = interpolateSnippet(fqn);
		return fqnToRelPathAndClean(expandedFqn);
	}

	private static String fqnToRelPathAndClean(String fqn){
		if(fqn.endsWith(".java")){
			fqn = fqn.substring(0, fqn.length() - 5);
		}
		String relPath = fqn.replace('.','/');
		checkValidRelPath(relPath);
//		if(relPath.startsWith("/")){
//			relPath = relPath.substring(1,relPath.length());
//		}
		return relPath;
	}
	

	private static String checkValidRelPath(String relPath){
		checkNotNull(StringUtils.trimToNull(relPath), "expect non null or empty relative path");

		char c;
		for (int i = 0; i < relPath.length(); i++) {
			c = relPath.charAt(i);
			if (!(Character.isJavaIdentifierPart(c) || c == '/')) {
				throw new IllegalArgumentException(
						String.format("Relative paths can only contain valid java identifiers and forward slashes. Instead found '%s', for relative path '%s'", c, relPath));
			}
		}
		return relPath;
	}

}