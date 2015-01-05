package org.codemucker.jmutate.generate;

import java.io.File;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.JMutateAppInfo;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.Annotations;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.transform.CleanImportsTransform;
import org.codemucker.jmutate.transform.InsertFieldTransform;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.IsGenerated;
import org.codemucker.lang.annotation.NotThreadSafe;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;

@NotThreadSafe
public class CodeGenMetaGenerator {

	public static final String CODE_GEN_INFO_CLASS_PKG = CodeGenMetaGenerator.class.getPackage().getName();
	public static final String CODE_GEN_INFO_CLASS_NAME = "CodeGenMeta";
	public static final String CODE_GEN_INFO_CLASS_FULLNAME = CODE_GEN_INFO_CLASS_PKG + "." + CODE_GEN_INFO_CLASS_NAME;
    
    private static final Logger log = LogManager.getLogger(CodeGenMetaGenerator.class);

	private final JMutateContext ctxt;
	private final Class<? extends CodeGenerator<?>> generator;
	private boolean exists;
	private final String constantField;
	
	public CodeGenMetaGenerator(JMutateContext ctxt,Class<? extends CodeGenerator<?>> generator){
		this.ctxt = ctxt;
		this.generator = generator;
		this.constantField = extractGenName(generator.getName());
	}
	
	private String extractGenName(String name) {
		String[] parts = name.split("\\.");
		int i = parts.length - 2;
		if (i < 0) {
			i = 0;
		}
		String genName = "";
		for (; i < parts.length; i++) {
			if(genName.length() > 0){
				genName += "_";
			}
			genName += parts[i].toUpperCase();
		}
		return genName;
	}

	public boolean isManagedByThis(AbstractTypeDeclaration type){
		return isManagedByThis(JType.from(type).getAnnotations());
	}
	
	public boolean isManagedByThis(MethodDeclaration method){
		return isManagedByThis(JMethod.from(method).getAnnotations());
	}
	
	public boolean isManagedByThis(FieldDeclaration field){
		return isManagedByThis(JField.from(field).getAnnotations());
	}
	
	public boolean isManagedByThis(Annotations annotations){
		JAnnotation anon = annotations.find(AJAnnotation.with().fullName(IsGenerated.class)).getFirstOrNull();
		
		if(anon != null){
			String generator = anon.getValueForAttribute("by", null);
			//String hash = anon.getValueForAttribute("sha1", null);
			
			if(generator != null && ( generator.equalsIgnoreCase(getFullConstantFieldPath()) || generator.contains(this.generator.getName()))){
				return true;
			}
		}
		return false;
	}
	
    public void addGeneratedMarkers(SourceTemplate template) {
    	createClassIfNotExists();
        template.var("by", getFullConstantFieldPath());
        template.pl("@" + javax.annotation.Generated.class.getName() + "(${by})");
        template.pl("@" + IsGenerated.class.getName() + "(by=${by})");
    }
    
    public void addGeneratedMarkers(FieldDeclaration field){
    	addGeneratedMarkers(field.modifiers(),field.getAST());
    }
    
    public void addGeneratedMarkers(MethodDeclaration method){
		addGeneratedMarkers(method.modifiers(),method.getAST());
    }
    
    public void addGeneratedMarkers(AbstractTypeDeclaration type){
		addGeneratedMarkers(type.modifiers(),type.getAST());
    }
    
    private void addGeneratedMarkers(List<IExtendedModifier> modifiers,AST ast){
    	createClassIfNotExists();
    	
		NormalAnnotation a = ast.newNormalAnnotation();
		a.setTypeName(ast.newName(IsGenerated.class.getName()));
		QualifiedName gen = ast.newQualifiedName(ast.newName(CODE_GEN_INFO_CLASS_FULLNAME), ast.newSimpleName(getConstantFieldName()));
		addValue(a,"by",gen);
		//addValue(a,"sha1","1234");
		modifiers.add(0,a);
    }
    
    private static void addValue(NormalAnnotation a,String name, Expression value){	
		MemberValuePair existing = null;
		for (Object v : a.values()) {
			if (v instanceof MemberValuePair) {
				MemberValuePair p = (MemberValuePair) v;
				if (name.equals(p.getName())) {
					existing = p;
					break;
				}
			}
		}
		if (existing != null) {
			existing.delete();
		}
		
		AST ast = a.getAST();
		MemberValuePair pair = ast.newMemberValuePair();
		pair.setName(ast.newSimpleName(name));
		
		pair.setValue(value);
		a.values().add(pair);
	}
    
    private void createClassIfNotExists() {
    	if(exists){
    		return;
    	}
		Root root;
		if (!ctxt.getProjectLayout().getMainSrcDirs().isEmpty()) {
			File dir = ctxt.getProjectLayout().getMainSrcDirs().iterator().next();
			root = new DirectoryRoot(dir, RootType.MAIN, RootContentType.SRC);
		} else {
			root = ctxt.getDefaultGenerationRoot();
		}
		String resourcePath =  CODE_GEN_INFO_CLASS_FULLNAME .replace('.', '/') + ".java";
    	RootResource resource = root.getResource(resourcePath);
    	JSourceFile source;
        if (resource.exists()) {
        	log.debug("source " + resource.getRelPath() + " exists, loading" );
        	source = ctxt.getOrLoadSource(resource);
        } else {
        	log.debug("source " + resource.getRelPath() + " does not exist, creating" );
        	source = ctxt.newSourceTemplate()
                .var("pkg", CODE_GEN_INFO_CLASS_PKG)
                .var("className", CODE_GEN_INFO_CLASS_NAME)
                .pl("package ${pkg};")
                .pl("public class ${className} {}")
                .asSourceFileSnippet(root);
           //TODO:maybe track automatically via template create method?
           ctxt.trackChanges(source);
        }
        JField field = ctxt.newSourceTemplate()
                .pl("public static final String " + getConstantFieldName() + "=\"" + JMutateAppInfo.all + ";generator=" + generator.getName() + "\";")
                .asJFieldSnippet();

        ctxt.obtain(InsertFieldTransform.class)
        	.target(source.getMainType())
        	.field(field)
        	.clashStrategy(ClashStrategy.REPLACE)
        	.transform();

        writeToDiskIfChanged(source);
        exists = true;
    }
    
    
    public String getFullConstantFieldPath(){
    	return CODE_GEN_INFO_CLASS_FULLNAME + "." + getConstantFieldName();
    }

    public String getConstantFieldName(){
    	return constantField;
    }
    
    private JSourceFile writeToDiskIfChanged(JSourceFile source) {
        if (source != null) {
            cleanupImports(source.getAstNode());
            source = source.asMutator(ctxt).writeModificationsToDisk();
        }
        return source;
    }

    private void cleanupImports(ASTNode node) {
        ctxt.obtain(CleanImportsTransform.class)
            .addMissingImports(true)
            .nodeToClean(node)
            .transform();
    }
}
