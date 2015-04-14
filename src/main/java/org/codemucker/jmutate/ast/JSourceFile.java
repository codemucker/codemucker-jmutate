package org.codemucker.jmutate.ast;

import static com.google.common.collect.Lists.newArrayList;
import static org.codemucker.lang.Check.checkNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codemucker.jfind.DefaultFindResult;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.matcher.AJSourceFile;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.lang.ClassNameUtil;
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
	/**
	 * The timestamp of the resource when it was read to create the source for this file. Used to detect changes to teh version on disk
	 */
	private final long originalTimestamp;
	private final long originalAstModificationCount;
    
	public JSourceFile(RootResource resource, CompilationUnit cu, CharSequence sourceCode, long originalTimestamp) {
		this.resource = checkNotNull("resource", resource);
		this.sourceCode = sourceCode;
		this.compilationUnitNode = cu;
		this.originalTimestamp = originalTimestamp;
		this.originalAstModificationCount = cu.getAST().modificationCount();
	}

	public static JSourceFile fromResource(RootResource resource, JAstParser parser){
		checkNotNull("resource", resource);
		checkNotNull("parser", parser);
		
		try {
		    //small chance things change between now and reading, but no different if
	        //changes made just _after_we read it	        
		    long lastModified = resource.getLastModified();	        
		    String src = resource.readAsString();
		    CompilationUnit cu = parser.parseCompilationUnit(src,resource);
		    return new JSourceFile(resource, cu, src, lastModified);
		} catch(IOException e){
			throw new JMutateException("Error reading resource contents " + resource,e);
		}		
	}
	
	public static JSourceFile fromSource(RootResource resource, CharSequence sourceCode,CompilationUnit cu){
        checkNotNull("resource", resource);
        return new JSourceFile(resource, cu, sourceCode, Root.TIMESTAMP_NOT_EXIST);
    }
	
	public static JSourceFile fromSource(RootResource resource, CharSequence sourceCode, JAstParser parser){
		checkNotNull("resource", resource);
		checkNotNull("parser", parser);
		
		CompilationUnit cu = parser.parseCompilationUnit(sourceCode,resource);
		return new JSourceFile(resource, cu, sourceCode, Root.TIMESTAMP_NOT_EXIST);
	}

	/**
	 * Determine whether this source is in sync with the resource it represents
	 */
	public boolean isInSyncWithResource(){
        return originalTimestamp != Root.TIMESTAMP_NOT_EXIST && getCompilationUnitNode().getAST().modificationCount() == originalAstModificationCount && getResource().getLastModified() == originalTimestamp;
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
    		throw new JMutateException("can't apply changes", e);
    	} catch (BadLocationException e) {
    		throw new JMutateException("can't apply changes", e);
    	}
    	String updatedSrc = doc.get();
    	return updatedSrc;
	}		
	
	/**
	 * Return the source as returned by the resource (so effectively latest on disk)
	 * @return
	 */
	public String getLatestSource(){
	    if(resource.exists()){
	        try {
                return resource.readAsString();
            } catch (IOException e) {
                throw new JMutateException("Error reading latest source",e);
            }
	    }
	    return null;
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
	
	public void accept(BaseSourceVisitor visitor) {
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
	/**
	 * Use the given mutation context to make changes to the current source file
	 * 
	 * @param ctxt
	 * @return
	 */
	public JSourceFileMutator asMutator(JMutateContext ctxt){
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
		JType type = getTopTypeWithNameOrNull(simpleName);
		if (type != null) {
			return type;
		}
		Collection<String> names = extractTopTypeNames(getTopTypes());
		throw new JMutateException("Can't find top level type named '%s' in resource '%s'. Found %s", simpleName, resource.getRelPath(), Arrays.toString(names.toArray()));
	}
	
	public JType getTopTypeWithNameOrNull(String simpleName){
		List<AbstractTypeDeclaration> types = getTopTypes();
		for( AbstractTypeDeclaration type:types){
			if(simpleName.equals(type.getName().getFullyQualifiedName())){ //fqn is actually just the shortname
				return JType.from(type);
			}
		}
		return null;
	}
	
	public JType getTypeWithName(Class<?> type) {
		return getTypeWithSimpleName(type.getSimpleName());
	}
	
	/**
	 * Look through all top level types and all their children for any type with the given name. 
	 */
	public JType getTypeWithSimpleName(final String simpleName) {
		Matcher<JType> matcher = new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.getSimpleName().equals(simpleName);
			}
		};
		List<JType> found = internalFindTypesMatching(matcher);
		
		if (found.size() > 1) {
			throw new JMutateException("Invalid source file, found more than one type with name '%s'", simpleName);
		}
		if (found.size() == 1) {
			return found.get(0);
		}
		throw new JMutateException("Could not find type with name '%s' in %s", simpleName, this);
	}

	public JType getTypeWithFullName(String fullName) {
		return getTypeWithFullName(AString.equalTo(fullName));
	}
	
	public JType getTypeWithFullName(final Matcher<String> nameMatcher) {
		Matcher<JType> matcher = new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return diag.tryMatch(this, found.getFullName(), nameMatcher);
			}
		};
		
		List<JType> found = internalFindTypesMatching(matcher);
		
		if (found.size() > 1) {
			throw new JMutateException("Invalid source file, found more than one type with name '%s'", nameMatcher);
		}
		if (found.size() == 1) {
			return found.get(0);
		}
		throw new JMutateException("Could not find type with name '%s' in %s", nameMatcher, this);
	}

		
	public FindResult<JType> findAllTypes(){
		return DefaultFindResult.from(internalFindTypesMatching(AJType.any()));
	}
	
	public FindResult<JType> findTypesMatching(Matcher<JType> matcher){
		return DefaultFindResult.from(internalFindTypesMatching(matcher));
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