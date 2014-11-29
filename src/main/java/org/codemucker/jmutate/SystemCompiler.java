package org.codemucker.jmutate;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.ast.JSourceFile;

import com.google.inject.Inject;

public class SystemCompiler implements JCompiler {

    private static final Logger log = LogManager.getLogger(SystemCompiler.class);
    
    private static final String PATH_SEP = System.getProperty("path.separator");
    private static final String NL = System.getProperty("line.separator");
    private final ResourceLoader defaultResourceLoader;
    private final String sourceVersion;
    private final String targetVersion;
    
    @Inject
    public SystemCompiler(ResourceLoader resourceLoader,ProjectOptions project) {
        this.defaultResourceLoader = resourceLoader;
        this.sourceVersion = project.getSourceVersion();
        this.targetVersion = project.getTargetVersion();
        
        if(ToolProvider.getSystemJavaCompiler() == null){
        	throw new UnsupportedOperationException("the current java runtime  tool provider does not provide a compiler (javax.tools.ToolProvider.getSystemJavaCompiler() is null)");
        }
    }

    @Override
    public Class<?> toCompiledClass(JSourceFile source) {
        return toCompiledClass(source, defaultResourceLoader);
    }

    @Override
    public Class<?> toCompiledClass(RootResource resource) {
        return toCompiledClass(resource, defaultResourceLoader);
    }

    @Override
    public Class<?> toCompiledClass(JSourceFile source, ResourceLoader resourceLoader) {
        if (!source.isInSyncWithResource()) {
            throw new JMutateException("Source file " + source.getResource().getFullPathInfo() + " has modifications which need to be written to disk first");
        }
        return toCompiledClass(source.getResource(), resourceLoader);
    }   

    @Override
    public Class<?> toCompiledClass(RootResource resource, ResourceLoader resourceLoader) {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        try {
            if(log.isDebugEnabled()){
                log.debug("compile resource " + resource);
                log.debug("compile source version " + sourceVersion);
                log.debug("compile target version " + targetVersion);
                log.trace("compile classpath roots:");
                for(Root root:resourceLoader.getAllRoots()){
                    log.trace("root " + root.getPathName());        
                }
                log.trace("source:\n------------------");
                try {
                    String src = IOUtils.toString(resource.getInputStream(),"UTF-8");
                    log.trace(src);
                } catch (IOException e) {
                    throw new JMutateException("Couldn't read src",e);
                }
                log.trace("\n---------------");
            }
            
            List<String> compilerOptions = new ArrayList<String>();
            compilerOptions.add("-classpath");
            compilerOptions.add(convertToClasspath(resourceLoader));
            //compilerOptions.add("-bootclasspath");
            //compilerOptions.add(convertToClasspath(resourceLoader));
            
//            compilerOptions.add("-source");
//            compilerOptions.add(sourceVersion);
//            compilerOptions.add("-target");
//            compilerOptions.add(targetVersion);
            //compilerOptions.add("-Werror");
            //compilerOptions.add("-verbose");

            //TODO:bootstrap classpath
            File pathToSource = new File(resource.getRoot().getPathName(), resource.getRelPath());
            StringWriter errOut = new StringWriter();
            Iterable<? extends JavaFileObject> filesToCompile = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(pathToSource.getAbsoluteFile()));
            JavaCompiler.CompilationTask compileTask = compiler.getTask(errOut, fileManager, diagnostics, compilerOptions, null, filesToCompile);
            // do the compile
            log.debug("running compile");
            if (compileTask.call()/* && diagnostics.getDiagnostics().size() == 0*/) {
                if(log.isDebugEnabled()){
                    log.debug("compiler output:" + errOut.toString());
                }
                String className = resourcePathToClassName(resource.getRelPath());
                // create a classloader which will load our compiled class.
                // Include all the dependencies in class path so we can compile,
                // including the package root of the source file
                List<URL> classloaderClasspath = new ArrayList<>();
                classloaderClasspath.add(resource.getRoot().toURL());
                classloaderClasspath.addAll(convertToUrls(resourceLoader));

                try (URLClassLoader classloader = new URLClassLoader(classloaderClasspath.toArray(new URL[] {}))) {
                    Class<?> compiledClass = classloader.loadClass(className);
                    loadDeclaredClasses(compiledClass);
                    return compiledClass;
                } catch (IOException e) {
                    throw new JMutateException("couldn't load compiled class '" + className + "' for " + resource, e);
                }
            } else {
                StringBuilder errMsg = new StringBuilder("Compile errors in '" + resource + "'");
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    errMsg.append(String.format("%n! Error on line %d, column %d, in %s", diagnostic.getLineNumber(),diagnostic.getColumnNumber(), toUri(diagnostic.getSource())));
                    errMsg.append(String.format("%n%s", diagnostic.getMessage(null)));                    
                }
                String errOutput = errOut.toString();
                if(errOutput.length() > 0){
                    errMsg.append(String.format("%nCompile output:%n%s", errOut.toString()));
                }
                String src = readSourcePrependLineNumbers(resource);
                errMsg.append(String.format("%nsource=%n-------------%n%s%n-----------------", src));
                for (Root root : resourceLoader.getAllRoots()) {
                    errMsg.append(String.format("%nUsing root %s", root));
                }
                throw new JMutateException(errMsg.toString());
            }
        } catch (ClassNotFoundException e) {
            throw new JMutateException("error converting " + resource + " to compiled class", e);
        } finally {
            IOUtils.closeQuietly(fileManager);
        }
    }
    
    private static URI toUri(FileObject fo){
        if(fo!= null){
            return fo.toUri();
        }
        return null;
    }

    //seem some error loading generated classes uness we do this
    private static void loadDeclaredClasses(Class<?> klass){
        for (Class<?> declaredClass : klass.getDeclaredClasses()) {
            loadDeclaredClasses(declaredClass);
        }
    }
    private static String readSourcePrependLineNumbers(RootResource resource) {
        StringBuilder sb = new StringBuilder();
        try {
            String src = resource.readAsString();
            String[] lines = src.split("\r\n|\n");
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) {
                    sb.append(NL);
                }
                appendLineNumber(i + 1, sb);
                sb.append(lines[i]);
            }
        } catch (IOException e) {
            sb.append(String.format("Couldn't read source, error %s%n", e.getMessage()));
        }
        return sb.toString();
    }

    private static void appendLineNumber(int lineNum, StringBuilder sb) {
        sb.append(lineNum).append('.');        
        if (lineNum < 10) {
            sb.append("    ");
        } else if (lineNum < 100) {
            sb.append("   ");
        } else if (lineNum < 1000) {
            sb.append("  ");
        } else if (lineNum < 10000) {
            sb.append(" ");
        }
        sb.append(" ");
    }

    private static String resourcePathToClassName(String relPath) {
        // TODO:check file extension? or do we on purpose not care to allow
        // other magic?
        if (relPath.endsWith("." + JAVA_CLASS_EXTENSION)) {
            throw new JMutateException("Invalid source file, you've tried loading a class file. Path '" + relPath + "'");
        }
        String name = FilenameUtils.removeExtension(relPath);
        name = FilenameUtils.separatorsToUnix(name).replace('/', '.').replace('\\', '.');
        if (name.startsWith(".")) {
            name = name.substring(1);
        }
        return name;
    }

    private static List<URL> convertToUrls(ResourceLoader loader) {
        List<URL> urls = new ArrayList<>();
        for (Root root : loader.getAllRoots()) {
            urls.add(root.toURL());
        }
        return urls;
    }

    private static String convertToClasspath(ResourceLoader loader) {
        StringBuilder classpath = new StringBuilder();
        for (Root root : loader.getAllRoots()) {
            if (classpath.length() > 0) {
                classpath.append(PATH_SEP);
            }
            classpath.append(root.getPathName());
        }
        return classpath.toString();
    }

}
