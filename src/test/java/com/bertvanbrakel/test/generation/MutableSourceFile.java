package com.bertvanbrakel.test.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import com.bertvanbrakel.lang.interpolator.Interpolator;
import com.bertvanbrakel.test.bean.builder.BeanGenerationException;
import com.bertvanbrakel.test.finder.SourceFile;

public class MutableSourceFile {
		private final SourceFile srcFile;
		private final AbstractTypeDeclaration type;
		private static final int NOT_FOUND = -1;
		
		public MutableSourceFile(SourceFile srcFile, AbstractTypeDeclaration type){
			this.srcFile = srcFile;
			this.type = type;
		}
		
		public SourceFile getSourceFile(){
			return srcFile;
		}

		private void beforeEdit(){
			
		}

		private void afterEdit(){
			
		}
	
		//TODO:move into SrcFile?
		public void writeChangesToSrcFile(){
			writeChangesToFile(srcFile.getPath());
		}
		
		public void writeChangesToFile(File f){
			Document doc = new Document(srcFile.readSource());
			TextEdit edits = srcFile.getCompilationUnit().rewrite(doc, null);
			 try {
	            UndoEdit undo = edits.apply(doc);
            } catch (MalformedTreeException e) {
            	throw new BeanGenerationException("can't apply changes",e);
            } catch (BadLocationException e) {
	           throw new BeanGenerationException("can't apply changes",e);
            }
			
			String updatedSrc = doc.get();
			//now write to file
			writeSrcToFile(updatedSrc,f);	
			
		}
		
		private static void writeSrcToFile(String src, File f){
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(f);
				IOUtils.write(src, fos);
			} catch (FileNotFoundException e) {
				throw new BeanGenerationException("Couldn't write source to file" + f.getAbsolutePath(), e);
            } catch (IOException e) {
	            throw new BeanGenerationException("Couldn't write source to file" + f.getAbsolutePath(), e);
            } finally {
				IOUtils.closeQuietly(fos);
			}
		}
		
		public AbstractTypeDeclaration getAstNode() {
        	return type;
        }

		public void addFieldSnippet(String fieldSnippet) {
			beforeEdit();
			
			String src = wrapSnippetInClassDeclaration(fieldSnippet);
			
			TypeDeclaration type = parseSnippetAsClass(src);
			FieldDeclaration field = type.getFields()[0];
			
			addToBodyAfterBefore(field, col(FieldDeclaration.class),col(MethodDeclaration.class,TypeDeclaration.class, EnumDeclaration.class));
			
			afterEdit();
		}
		
		public void addConstructorSnippet(String ctorSnippet) {
			beforeEdit();
			
			String src = wrapSnippetInClassDeclaration(ctorSnippet);
			
			TypeDeclaration type = parseSnippetAsClass(src);
			MethodDeclaration method = type.getMethods()[0];
			if (method.getReturnType2() != null) {
				throw new BeanGenerationException("Constructors should not have any return type. Constructor was %s", method);
			}
			if (!method.getName().getIdentifier().equals(type.getName().getIdentifier())) {
				throw new BeanGenerationException("Constructors should have the same name as the type. Expected name '%s' but got '%s'", type.getName().getIdentifier(), method.getName().getIdentifier());
			}
			
			method.setConstructor(true);
			
			addToBodyAfterBefore(method, col(FieldDeclaration.class, EnumDeclaration.class), col(TypeDeclaration.class));
			
			afterEdit();
		}
		
		public void addMethodSnippet(String methodDeclaration) {
			beforeEdit();
			
			String src = wrapSnippetInClassDeclaration(methodDeclaration);
			
			TypeDeclaration type = parseSnippetAsClass(src);
			MethodDeclaration method = type.getMethods()[0];
			method.setConstructor(false);
			
			addToBodyAfterBefore(method, col(MethodDeclaration.class, FieldDeclaration.class, EnumDeclaration.class), col(TypeDeclaration.class));
			
			afterEdit();
		}
		
		private String wrapSnippetInClassDeclaration(String snippetSrc) {
			String simpleClassName = this.type.getName().getIdentifier();
			String wrappedSrc = "class " + simpleClassName + "{" + snippetSrc + "}";
			return wrappedSrc;
		}
		
		public void addClassSnippet(String src) {
			beforeEdit();

			TypeDeclaration type = parseSnippetAsClass(src);
			
			addToBodyAfterBefore(type,col(FieldDeclaration.class,MethodDeclaration.class, EnumDeclaration.class, TypeDeclaration.class),col());
		}
		
		@SuppressWarnings("unchecked")
        private void addToBodyAfterBefore(ASTNode child, Collection<Class<?>> afterNodesOfType, Collection<Class<?>> beforeNodesOfType){
			ASTNode copy = ASTNode.copySubtree(getAst(), child);
			List<ASTNode> body = type.bodyDeclarations();
			int index = findIndexToInsertAt(body, afterNodesOfType, beforeNodesOfType);
			body.add(index, copy);
		}
		
		private AST getAst(){
			return getAstNode().getAST();
		}
		
		private int findIndexToInsertAt(List<ASTNode> nodes, Collection<Class<?>> afterNodesOfType, Collection<Class<?>> beforeNodesOfType){
			int index = findLastIndexOf(nodes, afterNodesOfType);
			if( index != NOT_FOUND){
				index++;
			} else {
				index = findFirstIndexOf(nodes, beforeNodesOfType);
			}
			if( index == NOT_FOUND){
				index = 0;
			}
			return index;
		}
		
		Collection<Class<?>> col(Class<?>...types){
			return Arrays.asList(types);
		}
		
		private int findLastIndexOf(List<ASTNode> nodes, Collection<Class<?>> nodeTypes){			
			int idx = 0;
			int last = NOT_FOUND;
			for( ASTNode node:nodes){
				if( nodeTypes.contains(node.getClass())){
					last = idx;
				}
				idx++;
			}
			return last;
		}

		private int findFirstIndexOf(List<ASTNode> nodes, Collection<Class<?>> nodeTypes){			
			int idx = 0;
			for( ASTNode node:nodes){
				if( nodeTypes.contains(node.getClass())){
					return idx;
				}
				idx++;
			}
			return NOT_FOUND;
		}

		public TypeDeclaration parseSnippetAsClass(String snippetSrc){
			CompilationUnit cu = parseSnippet(snippetSrc);
			TypeDeclaration type = (TypeDeclaration) cu.types().get(0);
		
			return type;
		}
		
		public CompilationUnit parseSnippet(String snippetSrc){
    		  //get template variables and interpolate
    		Map<String,Object> vars = new HashMap<String, Object>();
    		CharSequence src = Interpolator.interpolate(snippetSrc, vars);
    		//parse it
    		CompilationUnit cu = srcFile.getAstCreator().parseCompilationUnit(src);
    		return cu;
		}

		public boolean isStatic(){
			return Modifier.isStatic(getModifiers());
		}
		
		public boolean isFinal(){
			return Modifier.isFinal(getModifiers());
		}
		
		public boolean isPublic(){
			return Modifier.isPublic(getModifiers());
		}
		
		public boolean isAbstract(){
			return Modifier.isAbstract(getModifiers());
		}
		
		public int getModifiers(){
			//todo:may want to cache this as this is recalculated on each call
			//in which case e also want to know when they change so cache can be cleared
			return type.getModifiers();
		}
		
		public boolean isEnum(){
			return type instanceof EnumDeclaration;
		}
		
		public boolean isAnnotation(){
			return type instanceof AnnotationTypeDeclaration;
		}
		
		public boolean isInnerClass(){
			return isClass() && type.isMemberTypeDeclaration();
		}
		
		public boolean isTopLevelClass(){
			return type.isPackageMemberTypeDeclaration();
		}
		
		public boolean isConcreteClass(){
			return isClass() && !asType().isInterface();
		}
		
		public boolean isInterface(){
			return isClass() && asType().isInterface();
		}
		
		
//		public boolean isAnonymous(){
//			return isClass() && asType().;
//		}
//		
		public boolean isClass(){
			return type instanceof TypeDeclaration;
		}
		
		public TypeDeclaration asType(){
			return (TypeDeclaration)type;
		}
		
		public EnumDeclaration asEnum(){
			return (EnumDeclaration)type;
		}
		
		public AnnotationTypeDeclaration asAnnotation(){
			return (AnnotationTypeDeclaration)type;
		}
		
		public <A extends Annotation> boolean hasAnnotation(Class<A> anon){
			return getAnnotation(anon) != null;
		}
		public <A extends Annotation> A getAnnotation(Class<A> anon){
			
			return null;
		}		
	}