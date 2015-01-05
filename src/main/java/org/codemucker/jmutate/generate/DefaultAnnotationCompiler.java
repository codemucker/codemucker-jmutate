    package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.JMutateCompileException;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.BaseASTVisitor;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.ToSourceConverter;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class DefaultAnnotationCompiler  implements JAnnotationCompiler {
    
    private static String KEY = DefaultAnnotationCompiler.class.getName() + ":CompiledAnon";
    private static org.eclipse.jdt.core.dom.Annotation[] EMPTY = new org.eclipse.jdt.core.dom.Annotation[]{};
    
    private final String tmpPackageName;
    
    private final JMutateContext ctxt;

    /**
     * To ensure each compiled class has a unique name
     */
    private int uniqueSourceCount = 0;
    private final ToSourceConverter toSourceConverter;

    @Inject
    public DefaultAnnotationCompiler(JMutateContext ctxt) {
        super();
        this.ctxt = ctxt;
        //ensure there are no clashes with other annotation compilers running
        this.tmpPackageName = "org.codemucker.jmutate.generate.annotationcompiler.tmp" + hashCode();
        this.toSourceConverter = ctxt.getNodeToSourceConverter();
    }

    @Override
    public Annotation toCompiledAnnotation(org.eclipse.jdt.core.dom.Annotation astAnonNode) {
        Annotation compiledAnon = (Annotation) astAnonNode.getProperty(KEY);
        if (compiledAnon == null) {
            internalCompileAnnotations(astAnonNode);
            compiledAnon = (Annotation) astAnonNode.getProperty(KEY);
        }
        return compiledAnon;
    }

    @Override
    public void compileAnnotations(Iterable<org.eclipse.jdt.core.dom.Annotation> astNodes) {
    	//split into smaller chunks to remove failing compilations??
    	List<org.eclipse.jdt.core.dom.Annotation> copy = Lists.newArrayList(astNodes);
    	while(!copy.isEmpty()){
    		List<org.eclipse.jdt.core.dom.Annotation> chunk = new ArrayList<>(10);
    		for(int i = 0; !copy.isEmpty() && i < 10;i++){
    			chunk.add(copy.remove(copy.size() -1));
    		}
    	     internalCompileAnnotations(chunk.toArray(EMPTY));		
    	}
   //     internalCompileAnnotations(Lists.newArrayList(astNodes).toArray(EMPTY));
    }
    
    /**
     * Generates a new source file with all the provided annotations embedded at the correct location (type annotations on types, method annotations on methods etc)
     * and then compiles this source file. Once compiled the annotations are pulled out from the compiled class and cached in the corresponding ast node from where
     * it originally came from
     * 
     * <p>A single source file is created with all the given annotations so we don't need to incoke the compiler multiple times</p>
     * @param nodes
     */
    private void internalCompileAnnotations(org.eclipse.jdt.core.dom.Annotation... nodes) {
        Collection<CompiledAnnotationExtractor> extractors = new ArrayList<>();
        
        SourceTemplate t = ctxt.newTempSourceTemplate();
        t.var("pkg", tmpPackageName);
        t.pl("package ${pkg};");
        
        //add imports to ensure nested annotations also appear. TODO:clone annotation ast and convert to fully qualified names to prevent name clashes
        Collection<String> imports = collectImports(nodes);
        for (String imprt : imports) {
            t.pl("import " + imprt + ";");
        }
        //ensure each time we generate a source file we don't clash with a previous invocation
        uniqueSourceCount++;
        t.pl("public class TmpCompiledAnnotations" + uniqueSourceCount +"{");
        //ensure a unique name for each temporary class
        int typeNum = -1;
        for (org.eclipse.jdt.core.dom.Annotation node : nodes) {
            typeNum++;
            String typeName = "Type" + typeNum;
            String comment = "/*";
            RootResource resource = MutateUtil.getSource(node).getResource();
            if(resource!=null){
                comment += "from " + resource.getRoot().getFullPath() + "=>" + resource.getRelPath();
            }
            comment +="*/";
            
            String anonSrc = getSrcWithFullyQualifiedName(node);
            
            CompiledAnnotationExtractor extractor;
            switch (getAnnotationType(node)) {
            case INTERFACE:
                t.pl(comment);
                t.pl(anonSrc);
                t.pl("public interface ${type}{}","type",typeName);
                extractor = CompiledAnnotationExtractor.type(node,typeNum);
                break;
            case TYPE:
                t.pl(comment);
                t.pl(anonSrc);
                t.pl("public class ${type}{}","type",typeName);
                extractor = CompiledAnnotationExtractor.type(node,typeNum);
                break;
            case ANNOTATION:
                t.pl(comment);
                t.pl(anonSrc);
                t.pl("public @interface ${type}{}","type",typeName);
                extractor = CompiledAnnotationExtractor.type(node,typeNum);
                break;
            case FIELD:
                t.pl("public class ${type}{","type",typeName);
                t.pl(comment);
                t.pl("  " + anonSrc);
                t.pl("  String myField;");
                t.pl("}");
                extractor = CompiledAnnotationExtractor.field(node,typeNum,0);
                break;
            case CTOR:
                t.pl("public class ${type}{","type",typeName);
                t.pl(comment);
                t.pl("  " + anonSrc);
                t.pl("  ${type}(){}");
                t.pl("}");
                extractor = CompiledAnnotationExtractor.method(node,typeNum,0);
                break;
            case METHOD:
                t.pl("public interface ${type}{","type",typeName);
                t.pl(comment);
                t.pl("  " + anonSrc);
                t.pl("  void someMethod(){}");
                t.pl("}");
                extractor =  CompiledAnnotationExtractor.method(node,typeNum,0);
                break;
            case PARAM:
                t.pl("public interface ${type}{","type",typeName);
                t.pl(comment);
                t.pl("  void someMethod(${anon} String myParam){}","anon",anonSrc);
                t.pl("}");
                extractor = CompiledAnnotationExtractor.methodParam(node,typeNum,0,0);
                break;
            default:
                throw new JMutateException("Don't yet support compiling annotation:" + node);
            }
            extractors.add(extractor);
        }
        t.pl("}");
        Class<?> tmpContainingClass = toCompiledClass(t);
        for (CompiledAnnotationExtractor extractor : extractors) {
            try {
                extractor.extractAndCacheAnnotation(tmpContainingClass);
            } catch (NoClassDefFoundError | IndexOutOfBoundsException | JMutateException e) {
                Set<String> classpathEntries = toClassLoaderUrls(tmpContainingClass.getClassLoader());
                String msg = String.format("Couldn't extract compiled annotation from tmp class %s compiled from source %n-----------%n%s%n-----------%n using extractor %s with classpath entries %n%s", tmpContainingClass.getName(), t.interpolateTemplate(), extractor, Joiner.on('\n').join(classpathEntries));
                throw new JMutateCompileException(msg, e);
            }
        }
    }
    
    private static Set<String> toClassLoaderUrls(ClassLoader classloader){
        Set<String> urls = new TreeSet<>();//Sets.newLinkedHashSet();
        while (classloader != null) {
            if (classloader instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) classloader).getURLs()) {
                    urls.add(url.toExternalForm());
                    // System.out.println("Roots:url=" + url.getPath());
                }
            }
            classloader = classloader.getParent();
        }
        return urls;
    }

    private Collection<String> collectImports(org.eclipse.jdt.core.dom.Annotation... nodes) {
        ImportCollectorVisitor importCollector = new ImportCollectorVisitor();
        for (org.eclipse.jdt.core.dom.Annotation node : nodes) {
            node.accept(importCollector);
        }
        return importCollector.getCollectedImports();
    }

    private String getSrcWithFullyQualifiedName(org.eclipse.jdt.core.dom.Annotation node) {
        String anonSrc = toSourceConverter.toSource(node).trim();
        return anonSrc;
    }
    
    private Class<?> toCompiledClass(SourceTemplate template){
        JSourceFile  f = template.asSourceFileSnippet().asMutator(ctxt).writeModificationsToDisk();
        Class<?> compiledClass = ctxt.getCompiler().toCompiledClass(f);
        return compiledClass;
    }

    private static enum AnnotationType {
        FIELD, METHOD, TYPE, INTERFACE, CTOR, ANNOTATION, PARAM;
    }

    private DefaultAnnotationCompiler.AnnotationType getAnnotationType(org.eclipse.jdt.core.dom.Annotation node) {
        ASTNode parent = node.getParent();
        while (parent != null) {
            if (parent instanceof FieldDeclaration) {
                return AnnotationType.FIELD;
            } else if (parent instanceof MethodDeclaration) {
                if (((MethodDeclaration) parent).isConstructor()) {
                    return AnnotationType.CTOR;
                }
                return AnnotationType.METHOD;
            } else if (parent instanceof TypeDeclaration) {
                if (((TypeDeclaration) parent).isInterface()) {
                    return AnnotationType.INTERFACE;
                }
                return AnnotationType.TYPE;
            } else if (parent instanceof org.eclipse.jdt.core.dom.Annotation) {
                return AnnotationType.ANNOTATION;
            } else if (parent instanceof SingleVariableDeclaration) {
                return AnnotationType.PARAM;
            }

            parent = parent.getParent();
        }
        throw new JMutateException("Currently can't figure out the annotation type:" + node);
    }
    
    /**
     * Collects all the non qualified names from a node. This allows us to have nested annotations and still compile the code (by importing the types)
     */
    private static class ImportCollectorVisitor extends BaseASTVisitor {
        private  Set<String> imports = new TreeSet<>();
        
        Collection<String> getCollectedImports(){
            return imports;
        }
        
        @Override
        public boolean visit(MarkerAnnotation node) {
            add(node.getTypeName());
            return true;
        }
        
        @Override
        public boolean visit(NormalAnnotation node){
            add(node.getTypeName());
            return true;
        }
        
        @Override
        public boolean visit(SingleMemberAnnotation node) {
            add(node.getTypeName());
            return true;
        }
        
        @Override
        public boolean visit(SimpleType node) {
            add(node.getName());
            return true;
        }
        
        private void add(Name name){
            if(name.isSimpleName()){
                imports.add(NameUtil.resolveQualifiedName(name));
            }
        }
    }

    /**
     * Extracts the compiled annotation from a class
     */
    private static class CompiledAnnotationExtractor {
        /**
         * The index of the type holding the annotation
         */
        final int typeIndex;
        /**
         * The index of the method with the annotation, or -1 if not a method annotation.
         */
        final int methodIndex;
        /**
         * The index of the field or method parameter with the annotation. Is a field if the method index is -1, else a method param
         */
        final int fieldIndex;
        
        /**
         * The original source ast node of the compiled annotation
         */
        final org.eclipse.jdt.core.dom.Annotation astNode;

        public static CompiledAnnotationExtractor type(org.eclipse.jdt.core.dom.Annotation node, int typeNum) {
            return new CompiledAnnotationExtractor(node, typeNum, -1, -1);
        }

        public static CompiledAnnotationExtractor method(org.eclipse.jdt.core.dom.Annotation node, int typeNum, int methodNum) {
            return new CompiledAnnotationExtractor(node, typeNum, methodNum, -1);
        }

        public static CompiledAnnotationExtractor field(org.eclipse.jdt.core.dom.Annotation node, int typeNum, int fieldNum) {
            return new CompiledAnnotationExtractor(node, typeNum, 0, fieldNum);
        }

        public static CompiledAnnotationExtractor methodParam(org.eclipse.jdt.core.dom.Annotation node, int typeNum, int methodNum, int paramNum) {
            return new CompiledAnnotationExtractor(node, typeNum, 0, paramNum);
        }

        private CompiledAnnotationExtractor(org.eclipse.jdt.core.dom.Annotation node, int typeNum, int methodNum, int fieldNum) {
            super();
            this.typeIndex = typeNum;
            this.methodIndex = methodNum;
            this.fieldIndex = fieldNum;
            this.astNode = node;
        }

        private void extractAndCacheAnnotation(Class<?> tmpClass) {
            Annotation anon = extractAnnotation(tmpClass);
            astNode.setProperty(KEY, anon);
        }

        private Annotation extractAnnotation(Class<?> klass) {
            try {
                Class<?> type = klass.getDeclaredClasses()[typeIndex];
                if (methodIndex != -1) {
                    Method m = type.getDeclaredMethods()[methodIndex];
                    if (fieldIndex != -1) {
                        return m.getParameterAnnotations()[fieldIndex][0];
                    }
                    return m.getDeclaredAnnotations()[0];
                }
                if (fieldIndex != -1) {
                    return type.getDeclaredFields()[fieldIndex].getAnnotations()[0];
                }
                return type.getDeclaredAnnotations()[0];

            } catch (ArrayIndexOutOfBoundsException e) {
                StringBuilder msg = new StringBuilder();
                msg.append("Couldn't find the annotation to extract using :");
                msg.append(toString());
                extractInfo(klass,msg,0);
                
                throw new JMutateCompileException(msg.toString());
            }
        }
        
        private static void extractInfo(Class<?> klass, StringBuilder msg, int depth) {
            newlineAndPad(msg,depth);
            msg.append("for type " + klass.getSimpleName());

            newlineAndPad(msg,depth);
            Annotation[] annons = klass.getDeclaredAnnotations();
            msg.append("found " + annons.length + " annotations");

            newlineAndPad(msg,depth);
            Field[] fields = klass.getDeclaredFields();
            msg.append("found " + fields.length + " fields");
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                newlineAndPad(msg,depth +1);
                msg.append("field " + i + " has " + f.getDeclaredAnnotations().length + " annotations");
            }

            newlineAndPad(msg,depth);
            Method[] methods = klass.getDeclaredMethods();
            msg.append("found " + methods.length + " methods");
            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                newlineAndPad(msg,depth + 1);
                msg.append("method " + i + " has " + m.getDeclaredAnnotations().length + " annotations");
            }

            newlineAndPad(msg,depth);
            Class<?>[] types = klass.getDeclaredClasses();
            msg.append("found " + types.length + " embedded classes");
            for (int i = 0; i < types.length; i++) {
                Class<?> embeddedClass = types[i];
                extractInfo(embeddedClass, msg, depth + 1);
            }
        }
        
        private static void newlineAndPad(StringBuilder sb, int pad){
            sb.append("\n");
            for(int i=0;i<pad;i++){
                sb.append("..");
            }
        }
        
        @Override
        public String toString() {
            String s = super.toString() + "{";
            s += "type[" + typeIndex + "]";
            if (methodIndex != -1) {
                s += ",method[" + methodIndex + "]";
                if (fieldIndex != -1) {
                    s += ",methodParam[" + fieldIndex + "]";
                }
            } else if (fieldIndex != -1) {
                s += ",field[" + fieldIndex + "]";
            }
            s += ",annotation[0]";
            s += "}";
            return s;
        }
    }
}