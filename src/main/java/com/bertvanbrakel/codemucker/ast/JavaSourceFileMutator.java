package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import com.bertvanbrakel.codemucker.bean.BeanGenerationException;

public class JavaSourceFileMutator {
	private final JavaSourceFile srcFile;

	public JavaSourceFileMutator(JavaSourceFile srcFile) {
		checkNotNull("srcFile", srcFile);
		this.srcFile = srcFile;
	}
	
	public JavaSourceFile getJavaSourceFile() {
		return srcFile;
	}

	public CompilationUnit getCompilationUnit() {
		return srcFile.getCompilationUnit();
	}

	/**
	 * Return the type declared in this file with the same name as the name of the file.
	 */
	public JTypeMutator getMainTypeAsMutable() {
		return new JTypeMutator(srcFile.getMainType());
	}
	
	/**
	 * Return all the top level java types declared in this file
	 */
	public Iterable<JTypeMutator> getTypesAsMutable() {
		List<JTypeMutator> mutables = new ArrayList<JTypeMutator>();
		List<AbstractTypeDeclaration> types = getCompilationUnit().types();
		for (AbstractTypeDeclaration type : types) {
			mutables.add(new JTypeMutator(type));
		}
		return mutables;
	}

	public void writeChangesToSrcFile() {
		if( hasModifications() ){
			writeChangesToFile(srcFile.getLocation().getFile());
		}
	}

	public boolean hasModifications(){
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

}