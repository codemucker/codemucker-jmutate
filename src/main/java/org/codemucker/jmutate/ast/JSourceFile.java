package org.codemucker.jmutate.ast;

import static com.google.common.collect.Lists.newArrayList;
import static org.codemucker.lang.Check.checkNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.MutateContext;
import org.codemucker.jmutate.MutateException;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jtest.ClassNameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;


public class JSourceFile implements AstNodeProvider<CompilationUnit> {
	
	//TODO:cache calculated fields, but clear on node modification
	
	private final RootResource resource;
	private final CompilationUnit compilationUnitNode;
	private final CharSequence sourceCode;

	public JSourceFile(RootResource resource, CompilationUnit cu, CharSequence sourceCode) {
		this.resource = checkNotNull("resource", resource);
		this.sourceCode = sourceCode;
		this.compilationUnitNode = cu;
	}

	public static JSourceFile fromResource(RootResource resource, JAstParser parser){
		checkNotNull("resource", resource);
		checkNotNull("parser", parser);
		
		String src;
		try {
			src = resource.readAsString();
		} catch(IOException e){
			throw new MutateException("Error reading resource contents " + resource,e);
		}
		
		return fromSource(resource, src, parser);
	}
	
	public static JSourceFile fromSource(RootResource resource, CharSequence sourceCode, JAstParser parser){
		checkNotNull("resource", resource);
		checkNotNull("parser", parser);
		
		CompilationUnit cu = parser.parseCompilationUnit(sourceCode,resource);
		return new JSourceFile(resource, cu, sourceCode);
	}

	/**
	 * Write any modifications to the AST back to disk. May throw an exception if the resource is not modifiable
	 */
	public void writeModificationsToDisk() {
		if( hasModifications() ){
			internalWriteChangesToFile();
		}
	}

	public boolean hasModifications(){
		return getCompilationUnitNode().getAST().modificationCount() > 0;
	}

	private void internalWriteChangesToFile() {
		//TODO:check current contents of resource to validate no changes between the time we loaded
		//it and now?
		String src = getCurrentSource();
		OutputStream os = null;
		try {
			os = resource.getOutputStream();
			IOUtils.write(src, os);
		} catch (FileNotFoundException e) {
			throw new MutateException("Couldn't write source to file" + resource, e);
		} catch (IOException e) {
			throw new MutateException("Couldn't write source to file" + resource, e);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}
	
	/**
	 * Return the source as it would look with all the current modifications to the AST applied
	 */
	public String getCurrentSource(){
    	Document doc = new Document(getOriginalSource());
    	TextEdit edits = getCompilationUnitNode().rewrite(doc, null);
    	try {
    		edits.apply(doc);
    	} catch (MalformedTreeException e) {
    		throw new MutateException("can't apply changes", e);
    	} catch (BadLocationException e) {
    		throw new MutateException("can't apply changes", e);
    	}
    	String updatedSrc = doc.get();
    	return updatedSrc;
	}		
	
	/**
	 * Return the source as it was before any modifications were applied
	 */
	public String getOriginalSource() {
		return sourceCode==null?null:sourceCode.toString();
	}
	
	@Override
	public CompilationUnit getAstNode(){
		return compilationUnitNode;
	}
	
	public void visit(JFindVisitor visitor) {
		if (visitor.visit(this)) {
			CompilationUnit cu = getCompilationUnitNode();
			if (visitor.visit(cu)) {
				cu.accept(visitor);
			}
			visitor.endVisit(cu);
		}
		visitor.endVisit(this);
	}

	//TODO:code smell here, should JSource really know about the mutator? should the mutator use a builder instead?
	public JSourceFileMutator asMutator(MutateContext ctxt){
		return new JSourceFileMutator(ctxt, this);
	}
	
	/**
	 * Return the resource location of this source file. This could be an auto generated resource location
	 * @return
	 */
	public RootResource getResource(){
		return resource;
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
		return ClassNameUtil.upperFirstChar(resource.getBaseFileNamePart());
	}
	
	public JType getTopTypeWithName(Class<?> type){
		return getTopTypeWithName(type.getSimpleName());
	}
	/**
	 * Look through just the top level types for this file for a type with the given name
	 */
	public JType getTopTypeWithName(String simpleName){
		List<AbstractTypeDeclaration> types = getTopTypes();
		for( AbstractTypeDeclaration type:types){
			if(simpleName.equals(type.getName().getFullyQualifiedName())){ //fqn is actually just the shortname
				return JType.from(type);
			}
		}
		Collection<String> names = extractTopTypeNames(types);
		throw new MutateException("Can't find top level type named '%s' in resource '%s'. Found %s", simpleName, resource.getRelPath(), Arrays.toString(names.toArray()));
	}
	

	public JType getTypeWithName(Class<?> type) {
		return getTypeWithName(type.getSimpleName());
	}
	
	/**
	 * Look through all top level types and all their children for any type with the given name. 
	 */
	public JType getTypeWithName(final String simpleName) {
		Matcher<JType> matcher = new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.getSimpleName().equals(simpleName);
			}
		};
		List<JType> found = internalFindTypesMatching(matcher);
		if (found.size() > 1) {
			throw new MutateException("Invalid source file, found more than one type with name '%s'", simpleName);
		}
		if (found.size() == 1) {
			return found.get(0);
		}
		throw new MutateException("Could not find type with name '%s' in %s", simpleName, this);
	}

	public List<JType> findAllTypes(){
		return internalFindTypesMatching(AJType.any());
	}
	
	public List<JType> findTypesMatching(Matcher<JType> matcher){
		return internalFindTypesMatching(matcher);
	}
	
	private List<JType> internalFindTypesMatching(final Matcher<JType> matcher){
		final List<JType> found = newArrayList();
		ASTVisitor visitor = new BaseASTVisitor(){
			@Override
			protected boolean visitNode(ASTNode node) {
				if(JType.isTypeNode(node)){
					JType type = JType.from(node);
					if( matcher.matches(type)) {
						found.add(type);
					}
				}
				return true;
			}
		};
		compilationUnitNode.accept(visitor);
		return found;
	}

	private static List<String> extractTopTypeNames(List<AbstractTypeDeclaration> types){
		List<String> names = newArrayList();
		for(AbstractTypeDeclaration type:types){
			names.add(type.getName().toString());
		}
		return names;
	}

	public List<JType> getTopJTypes() {
		List<JType> javaTypes = newArrayList();
		for( AbstractTypeDeclaration type:getTopTypes()){
			javaTypes.add(JType.from(type));
		}
		return javaTypes;
	}
	
	@SuppressWarnings("unchecked")
	public List<AbstractTypeDeclaration> getTopTypes() {
		return getCompilationUnitNode().types();
	}

	public JCompilationUnit getCompilationUnit() {
		return JCompilationUnit.from(getCompilationUnitNode());
	}

	public CompilationUnit getCompilationUnitNode() {
		return compilationUnitNode;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}