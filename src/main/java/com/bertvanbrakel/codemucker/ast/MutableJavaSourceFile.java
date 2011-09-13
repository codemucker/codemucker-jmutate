package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
	
	private static AbstractTypeDeclaration extractMainType(CompilationUnit cu) {
		List<AbstractTypeDeclaration> types = cu.types();
		assertEquals(1, types.size());

		AbstractTypeDeclaration type = types.iterator().next();
		// System.out.println("type=" + type.getName());
		return type;
	}

	public JavaSourceFile getSourceFile() {
		return srcFile;
	}

	public CompilationUnit getCompilationUnit() {
		return srcFile.getCompilationUnit();
	}

	public MutableJavaType getMainTypeAsMutable() {
		return new MutableJavaType(this, extractMainType(getCompilationUnit()));
	}
	
	public Iterable<MutableJavaType> getMutableTypes() {
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