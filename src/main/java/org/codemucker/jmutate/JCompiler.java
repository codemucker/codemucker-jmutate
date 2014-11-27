package org.codemucker.jmutate;

import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.ast.JSourceFile;

import com.google.inject.ImplementedBy;

@ImplementedBy(EclipseCompiler.class)
public interface JCompiler {

    public static final String JAVA_SRC_EXTENSION = "java";
    public static final String JAVA_CLASS_EXTENSION = "class";

    Class<?> toCompiledClass(JSourceFile source);

    Class<?> toCompiledClass(RootResource resource);

    Class<?> toCompiledClass(JSourceFile source, ResourceLoader resourceLoader);

    Class<?> toCompiledClass(RootResource resource, ResourceLoader resourceLoader);

}
