package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.bertvanbrakel.codemucker.ast.finder.matcher.JTypeMatchers;
import com.bertvanbrakel.codemucker.bean.BeanGenerationException;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.bertvanbrakel.test.util.ClassNameUtil;

public class JSourceFile implements AstNodeProvider<CompilationUnit> {
	
	private final ClassPathResource resource;
	private final CompilationUnit compilationUnit;
	private final CharSequence sourceCode;

	public JSourceFile(ClassPathResource resource, CompilationUnit cu, CharSequence sourceCode) {
		this.resource = checkNotNull("resource", resource);
		this.sourceCode = sourceCode;
		this.compilationUnit = cu;
	}

	public static JSourceFile fromResource(ClassPathResource resource, ASTParser parser){
		checkNotNull("resource", resource);
		checkNotNull("parser", parser);
		
		String src;
		try {
			src = resource.readAsString();
		} catch(IOException e){
			throw new CodemuckerException("Error reading resource contents " + resource,e);
		}
		
		return fromSource(resource, src, parser);
	}
	
	public static JSourceFile fromSource(ClassPathResource resource, CharSequence sourceCode, ASTParser parser){
		checkNotNull("resource", resource);
		checkNotNull("parser", parser);
		
		CompilationUnit cu = JAstParser.newBuilder().setParser(parser).build().parseCompilationUnit(sourceCode);
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
		return getCompilationUnit().getAST().modificationCount() > 0;
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
			throw new CodemuckerException("Couldn't write source to file" + resource, e);
		} catch (IOException e) {
			throw new CodemuckerException("Couldn't write source to file" + resource, e);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}
	
	/**
	 * Return the source as it would look with all the current modifications to the AST applied
	 */
	public String getCurrentSource(){
    	Document doc = new Document(getOriginalSource());
    	TextEdit edits = getCompilationUnit().rewrite(doc, null);
    	try {
    		edits.apply(doc);
    	} catch (MalformedTreeException e) {
    		throw new BeanGenerationException("can't apply changes", e);
    	} catch (BadLocationException e) {
    		throw new BeanGenerationException("can't apply changes", e);
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

	//TODO:code smell here, should JSource really know about the mutator? should the mutator use a builder instead?
	public JSourceFileMutator asMutator(MutationContext ctxt){
		return new JSourceFileMutator(ctxt, this);
	}
	
	/**
	 * Return the resource location of this source file. This could be an auto generated resource location
	 * @return
	 */
	public ClassPathResource getResource(){
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
			//fqn is actually just the shortname
			if( simpleName.equals(type.getName().getFullyQualifiedName())){
				return new JType(type);
			}
		}
		Collection<String> names = extractNames(types);
		throw new CodemuckerException("Can't find top level type named '%s' in resource '%s'. Found %s", simpleName, resource.getRelPath(), Arrays.toString(names.toArray()));
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
		return internalFindTypesMatching(JTypeMatchers.any());
	}
	
	public List<JType> findTypesMatching(Matcher<JType> matcher){
		return internalFindTypesMatching(matcher);
	}
	
	private List<JType> internalFindTypesMatching(Matcher<JType> matcher){
		List<JType> found = newArrayList();
		for( JType type:getTopJTypes()){
			if( matcher.matches(type)){
				found.add(type);
			}
			type.findChildTypesMatching(matcher, found);
		}
		return found;
	}

	private static List<String> extractNames(List<AbstractTypeDeclaration> types){
		List<String> names = newArrayList();
		for( AbstractTypeDeclaration type:types){
			names.add(type.getName().toString());
		}
		return names;
	}

	public List<JType> getTopJTypes() {
		List<JType> javaTypes = newArrayList();
		for( AbstractTypeDeclaration type:getTopTypes()){
			javaTypes.add(new JType(type));
		}
		return javaTypes;
	}
	
	@SuppressWarnings("unchecked")
	public List<AbstractTypeDeclaration> getTopTypes() {
		return getCompilationUnit().types();
	}
	
	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}