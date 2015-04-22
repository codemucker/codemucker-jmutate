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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.JCompiler;
import org.codemucker.jmutate.JMutateCompileException;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.BaseASTVisitor;
import org.codemucker.jmutate.ast.JAstFlattener;
import org.codemucker.jmutate.ast.JCompilationUnit;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
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
    
    private static final Logger log = LogManager.getLogger(DefaultAnnotationCompiler.class);
    
    private final String tmpPackageName;
    
    private final JMutateContext ctxt;

    private final JCompiler compiler;
    /**
     * To ensure each compiled class has a unique name
     */
    private int uniqueSourceCount = 0;

    @Inject
    public DefaultAnnotationCompiler(JMutateContext ctxt,JCompiler compiler) {
        super();
        this.ctxt = ctxt;
        this.compiler = compiler;
        //ensure there are no clashes with other annotation compilers running
        this.tmpPackageName = "org.codemucker.jmutate.generate.annotationcompiler.tmp" + hashCode();
    }

    @Override
    public Annotation toCompiledAnnotation(org.eclipse.jdt.core.dom.Annotation node) {
        Annotation compiledAnon = (Annotation) node.getProperty(KEY);
        if (compiledAnon == null) {
            internalCompileAnnotations(node);
            compiledAnon = (Annotation) node.getProperty(KEY);
        }
        return compiledAnon;
    }

    @Override
    public void compileAnnotations(Iterable<org.eclipse.jdt.core.dom.Annotation> nodes) {
    	//split into smaller chunks to compile at a time else it appears annotations are not loaded
    	List<org.eclipse.jdt.core.dom.Annotation> list = Lists.newArrayList(nodes);
    	final int maxNum = 10;//any more than about this seems to use a different classloader and a class cast error!????!!
    	for(int start = 0; start < list.size();){
			int end = start + maxNum;
			if(end > list.size()){
				end = list.size();
			}
			//compile chunk
			org.eclipse.jdt.core.dom.Annotation[] chunk = list.subList(start, end).toArray(EMPTY);
			internalCompileAnnotations(chunk);
			start = end;
		}
    }
    
    /**
     * Generates a new source file with all the provided annotations embedded at the correct location (type annotations on types, method annotations on methods etc)
     * and then compiles this source file. Once compiled the annotations are pulled out from the compiled class and cached in the corresponding ast node from where
     * it originally came from
     * 
     * <p>A single source file is created with all the given annotations so we don't need to invoke the compiler multiple times</p>
     * @param nodes
     */
    private void internalCompileAnnotations(org.eclipse.jdt.core.dom.Annotation... nodes) {
        Collection<CompiledAnnotationExtractor> extractors = new ArrayList<>();
        
        //TODO:if annotations are the same (or at the very least using the empty version), use a single one across all nodes!
        SourceTemplate t = ctxt.newTempSourceTemplate();
        t.var("pkg", tmpPackageName);
        t.pl("package ${pkg};");

        //ensure each time we generate a source file we don't clash with a previous invocation
        uniqueSourceCount++;
        t.pl("public class TmpCompiledAnnotations" + uniqueSourceCount +"{");
        //ensure a unique name for each temporary class
        int typeNum = -1;
        for (org.eclipse.jdt.core.dom.Annotation node : nodes) {
        	NodeInfo info = getAnnotationType(node);
        
            typeNum++;
            String typeName = "Type" + typeNum;
            String comment = "/*";
            RootResource resource = MutateUtil.getSource(node).getResource();
            if(resource!=null){
                comment += "from " + resource.getRoot().getFullPath() + "=>" + resource.getRelPath() + "=>" + info.label;
            }
            comment +="*/";
            
            String anonSrc = getSrcWithFullyQualifiedName(node);
            
            CompiledAnnotationExtractor extractor;
            
            switch (info.type) {
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
                t.pl("  ${type}(){}","type",typeName);
                t.pl("}");
                extractor = CompiledAnnotationExtractor.method(node,typeNum,0);
                break;
            case METHOD:
                t.pl("public interface ${type}{","type",typeName);
                t.pl(comment);
                t.pl("  " + anonSrc);
                t.pl("  void someMethod();");
                t.pl("}");
                extractor =  CompiledAnnotationExtractor.method(node,typeNum,0);
                break;
            case PARAM:
                t.pl("public interface ${type}{","type",typeName);
                t.pl(comment);
                t.pl("  void someMethod(${anon} String myParam);","anon",anonSrc);
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
                String msg = String.format("Couldn't extract compiled annotation " + extractor.getAnnotationName() + ".%nHave you ensured the annotation has rentention policy of 'source'? is it public?%nTemporary class %s compiled from source %n-----------%n%s%n-----------%n using extractor %s with classpath entries %n%s", tmpContainingClass.getName(), t.interpolateTemplate(), extractor, Joiner.on('\n').join(classpathEntries));
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

    private String getSrcWithFullyQualifiedName(org.eclipse.jdt.core.dom.Annotation node) {

    	FullNameMarkerVisitor marker = new FullNameMarkerVisitor();
    	node.accept(marker);
    	
    	JAstFlattener toSourceVisitor = new JAstFlattener(){
    		@Override
			public boolean visit(SimpleName node) {
				String fullName = (String) node.getProperty(FullNameMarkerVisitor.PROP_FULL);
				if (fullName != null) {
					getBuffer().append(fullName);
					return false;
				} else {
					return super.visit(node);
				}
			}
    	};
    	node.accept(toSourceVisitor);
    	
    	String anonSrc = toSourceVisitor.getResult();
    	return anonSrc;
    }
    
    private Class<?> toCompiledClass(SourceTemplate template){
        JSourceFile source = template.asSourceFileSnippet().asMutator(ctxt).writeModificationsToDisk();
        Class<?> compiledClass = compiler.toCompiledClass(source);
        return compiledClass;
    }

    private static enum AnnotationType {
        FIELD, METHOD, TYPE, INTERFACE, CTOR, ANNOTATION, PARAM;
    }

    private static class NodeInfo {
    	public final AnnotationType type;
    	public final String label;
		public NodeInfo(AnnotationType type, String label) {
			super();
			this.type = type;
			this.label = label;
		}
    }
    private NodeInfo getAnnotationType(org.eclipse.jdt.core.dom.Annotation node) {
        ASTNode parent = node.getParent();
        while (parent != null) {
            if (parent instanceof FieldDeclaration) {
            	JField f = JField.from(parent);
             	String path = getTypePath(f.getEnclosingType()) + "." + f.getName();
                return new NodeInfo(AnnotationType.FIELD, path);
            } else if (parent instanceof MethodDeclaration) {
            	JMethod m = JMethod.from(parent);
            	String path = getTypePath(m.getDeclaringTypeNode()) + "." + m.getClashDetectionSignature();
                if (((MethodDeclaration) parent).isConstructor()) {
                    return new NodeInfo(AnnotationType.CTOR, path);
                }
                return new NodeInfo(AnnotationType.METHOD,path);
            } else if (parent instanceof TypeDeclaration) {
            	String path = getTypePath((AbstractTypeDeclaration) parent);
                if (((TypeDeclaration) parent).isInterface()) {
                	return new NodeInfo(AnnotationType.INTERFACE,path);
                }
                return new NodeInfo(AnnotationType.TYPE,path);
            } else if (parent instanceof org.eclipse.jdt.core.dom.Annotation || parent instanceof AnnotationTypeDeclaration) {
                return new NodeInfo(AnnotationType.ANNOTATION,getTypePath(parent));
            } else if (parent instanceof SingleVariableDeclaration) {
            	return new NodeInfo(AnnotationType.PARAM,getTypePath(parent));
            }
            parent = parent.getParent();
        }
        JCompilationUnit jcu = JCompilationUnit.from(JCompilationUnit.findCompilationUnitNode(node));
        
        throw new JMutateException("Currently can't figure out the annotation type:" + node + ", declared in " + jcu.getResource().getFullPath());
    }
 
    private static String getTypePath(ASTNode node){
    	String label = "";
    	ASTNode parent = node;
    	while(parent != null){
    		if(parent instanceof AbstractTypeDeclaration){
    			if(label.length() == 0){
    				label = ((AbstractTypeDeclaration)parent).getName().getIdentifier();
    			} else {
    				label = ((AbstractTypeDeclaration)parent).getName().getIdentifier() + "." + label;
    			}
    		}
    		parent = parent.getParent();
    	}
    	return label;
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

        public String getAnnotationName(){
        	String fullName = NameUtil.resolveQualifiedNameOrNull(astNode.getTypeName());
        	if(fullName == null){
        		fullName = astNode.getTypeName().getFullyQualifiedName();
        	}
        	return fullName;
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
            Annotation[] annons = klass.getAnnotations();
            msg.append("found " + annons.length + " annotations");
            
            newlineAndPad(msg,depth);
            Annotation[] annonsDeclared = klass.getDeclaredAnnotations();
            msg.append("found " + annonsDeclared.length + " annotations (declared)");

            newlineAndPad(msg,depth);
            Field[] fields = klass.getDeclaredFields();
            msg.append("found " + fields.length + " fields");
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                newlineAndPad(msg,depth +1);
                msg.append("field " + i + " '" + f.getName() + "' has " + f.getDeclaredAnnotations().length + " annotations");
            }

            newlineAndPad(msg,depth);
            Method[] methods = klass.getDeclaredMethods();
            msg.append("found " + methods.length + " methods");
            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                newlineAndPad(msg,depth + 1);
                msg.append("method " + i + " '" + m.getName() + "' has " + m.getDeclaredAnnotations().length + " annotations");
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
    
    /**
     * Marks nodes which need fullname expansion (when converting to source)
     */
	private static class FullNameMarkerVisitor extends BaseASTVisitor {

		public static final String PROP_FULL = FullNameMarkerVisitor.class.getName() + ":fullname";

		@Override
		public boolean visit(ImportDeclaration node) {
			//ignore
			return false;
		};
		
		@Override
        public boolean visit(PackageDeclaration node) {
            //ignore
            return false;
        };

        @Override
        public boolean visit(SimpleType node) {
            markWithFullName(node.getName(),NameUtil.resolveQualifiedName(node));
            return false;
        }
        
        @Override
        public boolean visit(MarkerAnnotation node){
        	markWithFullName(node.getTypeName(),NameUtil.resolveQualifiedName(node.getTypeName()));
        	return super.visit(node);
        }
        
        @Override
        public boolean visit(SingleMemberAnnotation node){
        	markWithFullName(node.getTypeName(),NameUtil.resolveQualifiedName(node.getTypeName()));
        	return super.visit(node);
        }
        
        @Override
        public boolean visit(NormalAnnotation node){
        	markWithFullName(node.getTypeName(),NameUtil.resolveQualifiedName(node.getTypeName()));
        	return super.visit(node);
        }
        
        @Override
        public boolean visit(MemberValuePair node){
        	Expression exp = node.getValue();
        	if(exp instanceof QualifiedName){
        		QualifiedName qn = (QualifiedName)exp;
        		Name qualifier = qn.getQualifier();
        		markWithFullName(qualifier,NameUtil.resolveQualifiedName(qualifier));
        	}
        	return super.visit(node);
        }
        
        private boolean markWithFullName(ASTNode node, String fullName) {
            //System.out.println(FullNameMarkerVisitor.class.getName() + ":fullname=" + fullName + ",for node:" + node.getClass().getName());
            node.setProperty(PROP_FULL, fullName);
            
            return false;
        }
	}
	
}