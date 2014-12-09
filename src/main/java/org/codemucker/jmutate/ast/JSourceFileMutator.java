package org.codemucker.jmutate.ast;

import static com.google.common.collect.Lists.newArrayList;
import static org.codemucker.lang.Check.checkNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.JMutateParseException;
import org.codemucker.jmutate.ast.ToSourceConverter.Kind;
import org.codemucker.jmutate.transform.CleanImportsTransform;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class JSourceFileMutator {
    
    private static final Logger log = LogManager.getLogger(JSourceFileMutator.class);
    
	private final JSourceFile source;
	private final JMutateContext context;

	public JSourceFileMutator(JMutateContext context, JSourceFile source) {;
		this.context = checkNotNull("context", context);
		this.source = checkNotNull("source", source);
	}
	
	public JSourceFile getJSource() {
		return source;
	}

	public CompilationUnit getCompilationUnit() {
		return source.getCompilationUnitNode();
	}

	public JCompilationUnit getJCompilationUnit() {
		return JCompilationUnit.from(source.getCompilationUnitNode());
	}

	/**
	 * Return the type declared in this file with the same name as the name of the file.
	 */
	public JTypeMutator getMainTypeAsMutable() {
		return new JTypeMutator(context, source.getMainType());
	}
	
	/**
	 * Return all the top level java types declared in this file
	 */
	public Iterable<JTypeMutator> getTypesAsMutable() {
		List<JTypeMutator> mutables = newArrayList();
		List<AbstractTypeDeclaration> types = getCompilationUnit().types();
		for (AbstractTypeDeclaration type : types) {
			mutables.add(new JTypeMutator(context, type));
		}
		return mutables;
	}
	
    /**
     * Write any modifications to the AST back to disk. May throw an exception if the resource is not modifiable
     */
	public JSourceFile writeModificationsToDisk() {
	    return writeModificationsToDisk(true);
	}
	
	public JSourceFile writeModificationsToDisk(boolean compareAstNodes) {
	    RootResource resource = source.getResource();
	    if(!sourcesMatch(compareAstNodes)){
            internalWriteChangesToFile(true);
            //reload source, also resets modification count
            return JSourceFile.fromResource(source.getResource(), context.getParser());
        } else {
            log.debug("no ast changes to write to disk, source in sync, for " + resource);
        }
        return source;
    }
	
	private boolean sourcesMatch(boolean compareAstNodes){
	    RootResource resource = source.getResource();
	    if(isInSyncWithResource()){
	        return true;
	    }
        if(compareAstNodes && resource.exists()){
            try {
                String existingOnDiskSrc= source.getResource().readAsString();
                try {
                    CompilationUnit existingOnDiskCu= context.getParser().parseCompilationUnit(existingOnDiskSrc, resource);
                    ASTMatcher matcher = JAstMatcher.with().matchDocTags(false).buildNonAsserting();
                    boolean theSame = matcher.match(getCompilationUnit(), existingOnDiskCu); 
                    return theSame;
                } catch(JMutateParseException e){
                    log.debug("couldn't parse existing source, going to replace it");
                    return false;
                }
            } catch (IOException e) {
                log.debug("couldn't read existing source, going to replace it");
            }
        }
        return false;
	}

    public boolean isInSyncWithResource(){
        return source.isInSyncWithResource();
    }
    
    private void internalWriteChangesToFile(boolean formatSrc) {
        String src = source.getCurrentSource();
        if(formatSrc){
            src = context.getNodeToSourceConverter().toFormattedSource(src, Kind.COMPILATION_UNIT);
        }
        OutputStream os = null;
        try {
            os = source.getResource().getOutputStream();
            IOUtils.write(src, os);
        } catch (FileNotFoundException e) {
            throw new JMutateException("Couldn't write source to " + source.getResource(), e);
        } catch (IOException e) {
            throw new JMutateException("Couldn't write source to " + source.getResource(), e);
        } finally {
            IOUtils.closeQuietly(os);
        }
        log.debug("wrote changes to write to disk for source " + source.getResource());
    }
    
}