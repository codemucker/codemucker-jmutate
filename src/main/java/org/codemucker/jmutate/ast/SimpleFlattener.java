package org.codemucker.jmutate.ast;

import static org.codemucker.lang.Check.checkNotNull;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.google.inject.Inject;

public class SimpleFlattener implements AstNodeFlattener {
	
	private CodeFormatter formatter;
	
	@Inject
	public SimpleFlattener(CodeFormatter formatter) {
		this.formatter = checkNotNull("formatter", formatter);
	}

	@Override
	public String flatten(ASTNode node) {
		String src = JAstFlattener.asString(node);
		String formatted = format(src, getKindForNode(node));
	
		return formatted;
	}

	private String format(String src, int kind){
		String nl = System.getProperty("line.seperator");
		int startIndentLevel = 0;
		TextEdit edits  = formatter.format(kind, src, 0, src.length(), startIndentLevel, nl);
		String formattedSource = applyTextEditsToSrc(src, edits);
		return formattedSource;
	}
	
	/**
	 * Return the resulting source from applying the given text edits to the given source
	 * @param edits
	 * @param originalSource
	 * @return the resulting new source
	 */
	private static String applyTextEditsToSrc(String originalSource,TextEdit edits){
		Document doc = new Document(originalSource);
    	try {
    		edits.apply(doc);
    	} catch (MalformedTreeException e) {
    		throw new CodemuckerException("can't apply changes", e);
    	} catch (BadLocationException e) {
    		throw new CodemuckerException("can't apply changes", e);
    	}
    
    	String updatedSrc = doc.get();
    	return updatedSrc;
	}
	
	private static int getKindForNode(ASTNode node){
		if( node instanceof CompilationUnit){
			return CodeFormatter.K_COMPILATION_UNIT;
		}
		return CodeFormatter.K_UNKNOWN;
	}
}