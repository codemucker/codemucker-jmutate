package org.codemucker.jmutate.ast;

import static org.codemucker.lang.Check.checkNotNull;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.google.inject.Inject;

public class DefaultToSourceConverter implements ToSourceConverter {
	
    private final Logger log = LogManager.getLogger(DefaultToSourceConverter.class);
    private static final String NL =  System.getProperty("line.seperator");
    
	private CodeFormatter formatter;
	
	@Inject
	public DefaultToSourceConverter(CodeFormatter formatter) {
		this.formatter = checkNotNull("formatter", formatter);
	}

	@Override
	public String toSource(ASTNode node) {
		String src = JAstFlattener.asString(node);
		return toFormattedSource(src, getKindForNode(node));
	}

	@Override
    public String toFormattedSource(String src, Kind kind) {
	    String formattedSource = src;
	    
		int startIndentLevel = 0;
		TextEdit edits  = formatter.format(kind.getCodeFormatterKind(), src, 0, src.length(), startIndentLevel, NL);
        if (edits == null) {
            String msg = String.format("Can not format the provided source, returning source as is. Source is %n%s", src);
            // throw new JMutateParseException(msg);
            log.warn(msg);
            formattedSource = applyTextEditsToSrc(src, edits);
        }
		 
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
		if(edits != null){
        	try {
        		edits.apply(doc);
        	} catch (MalformedTreeException e) {
        		throw new JMutateException("can't apply changes", e);
        	} catch (BadLocationException e) {
        		throw new JMutateException("can't apply changes", e);
        	}
		}
    
    	String updatedSrc = doc.get();
    	return updatedSrc;
	}
	
	private static Kind getKindForNode(ASTNode node){
		if( node instanceof CompilationUnit){
			return Kind.COMPILATION_UNIT;
		}
		return Kind.UNKNOWN;
	}

    
}