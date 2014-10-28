package org.codemucker.jmutate.ast;

import static com.google.common.collect.Lists.newArrayList;
import static org.codemucker.lang.Check.checkNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class JSourceFileMutator {
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
        if(!isInSyncWithResource()){
            internalWriteChangesToFile();
            //TODO:reload source?
            return JSourceFile.fromResource(source.getResource(), context.getParser());
        }
        return source;
    }

    public boolean isInSyncWithResource(){
        return source.isInSyncWithResource();
    }
    
    private void internalWriteChangesToFile() {
        
        String src = source.getCurrentSource();
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
    }
    
}