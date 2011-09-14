package com.bertvanbrakel.codemucker.ast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import com.bertvanbrakel.codemucker.bean.BeanGenerationException;
import com.bertvanbrakel.lang.interpolator.Interpolator;

public class MutableJavaSourceFile {
	private final JavaSourceFile srcFile;

	public MutableJavaSourceFile(JavaSourceFile srcFile) {
		this.srcFile = srcFile;
	}
	
	public JavaSourceFile getSourceFile() {
		return srcFile;
	}

	public CompilationUnit getCompilationUnit() {
		return srcFile.getCompilationUnit();
	}

	public MutableJavaType getMainTypeAsMutable() {
		return new MutableJavaType(this, getMainType());
	}
	
	public AbstractTypeDeclaration getMainType() {
		CompilationUnit cu = getCompilationUnit();
		List<AbstractTypeDeclaration> types = cu.types();
		String className = srcFile.getSimpleClassnameBasedOnPath();
		for( AbstractTypeDeclaration type:types){
			if( className.equals(type.getName().toString())){
				return type;
			}
		}
		Collection<String> names = extractNames(types);
		throw new CodemuckerException("Can't find main type in %s. Looking for %s. Found %s", srcFile.getLocation().getRelativePath(), className, Arrays.toString(names.toArray()));
	}

	private static Collection<String> extractNames(List<AbstractTypeDeclaration> types){
		Collection<String> names = new ArrayList<String>();
		for( AbstractTypeDeclaration type:types){
			names.add(type.getName().toString());
		}
		return names;
	}
	
	public Iterable<MutableJavaType> getTypesAsMutable() {
		List<MutableJavaType> mutables = new ArrayList<MutableJavaType>();
		List<AbstractTypeDeclaration> types = getCompilationUnit().types();
		for (AbstractTypeDeclaration type : types) {
			mutables.add(new MutableJavaType(this, type));
		}
		return mutables;
	}

	// TODO:move into SrcFile?
	public void writeChangesToSrcFile() {
		if( hasChanges() ){
			writeChangesToFile(srcFile.getLocation().getFile());
		}
	}

	public boolean hasChanges(){
		return getCompilationUnit().getAST().modificationCount() > 0;
	}
	
	public void writeChangesToFile(File f) {
		Document doc = new Document(srcFile.readSource());
		TextEdit edits = getCompilationUnit().rewrite(doc, null);
		try {
			UndoEdit undo = edits.apply(doc);
		} catch (MalformedTreeException e) {
			throw new BeanGenerationException("can't apply changes", e);
		} catch (BadLocationException e) {
			throw new BeanGenerationException("can't apply changes", e);
		}

		String updatedSrc = doc.get();
		// now write to file
		writeSrcToFile(updatedSrc, f);
	}

	private static void writeSrcToFile(String src, File f) {
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

	public TypeDeclaration parseSnippetAsClass(String snippetSrc) {
		CompilationUnit cu = parseSnippet(snippetSrc);
		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);

		return type;
	}

	public CompilationUnit parseSnippet(String snippetSrc) {
		// get template variables and interpolate
		Map<String, Object> vars = new HashMap<String, Object>();
		CharSequence src = Interpolator.interpolate(snippetSrc, vars);
		// parse it
		CompilationUnit cu = srcFile.getAstCreator().parseCompilationUnit(src);
		return cu;
	}
}