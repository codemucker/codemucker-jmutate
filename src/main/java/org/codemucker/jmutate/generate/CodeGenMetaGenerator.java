package org.codemucker.jmutate.generate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmutate.JMutateAppInfo;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.ResourceLoader;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.Annotations;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.transform.CleanImportsTransform;
import org.codemucker.jmutate.transform.InsertFieldTransform;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.DontGenerate;
import org.codemucker.jpattern.generate.IsGenerated;
import org.codemucker.lang.ClassNameUtil;
import org.codemucker.lang.annotation.NotThreadSafe;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

@NotThreadSafe
public class CodeGenMetaGenerator {

	public static final String CODE_GEN_INFO_CLASS_PKG = CodeGenMetaGenerator.class.getPackage().getName();
	public static final String CODE_GEN_INFO_CLASS_NAME = "CodeGenMeta";
	public static final String CODE_GEN_INFO_CLASS_FULLNAME = CODE_GEN_INFO_CLASS_PKG + "." + CODE_GEN_INFO_CLASS_NAME;
    
    private static final Logger log = LogManager.getLogger(CodeGenMetaGenerator.class);

    private static final String PROP_BY = "by";//on the IsGenerated class
	private final JMutateContext ctxt;
	private final Class<? extends CodeGenerator> generator;
	private boolean exists;
	private final String constantField;
	private final Map<String,Boolean> cachedFieldValueMatchResults = new HashMap<>();
	
	public CodeGenMetaGenerator(JMutateContext ctxt,Class<? extends CodeGenerator> generator){
		this.ctxt = ctxt;
		this.generator = generator;
		this.constantField = extractGenName(generator.getName());
	}
	
	private String extractGenName(String name) {
		String className = ClassNameUtil.extractSimpleClassNamePart(name);
		name = Character.toLowerCase(className.charAt(0)) + className.substring(1);
//		
//		StringBuilder sb = new StringBuilder();
//		boolean previousCharUpper = false;
//		for(int i = 0; i < className.length();i++){
//			char c = className.charAt(i);
//			boolean isUpper = Character.isUpperCase(c);
//			if(isUpper && i > 0 && !previousCharUpper){
//				sb.append("_");
//			}
//			sb.append(Character.toUpperCase(c));
//			previousCharUpper = isUpper;
//		}
//		return sb.toString();
		return name;
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
		JAnnotation dontGenerate = annotations.find(AJAnnotation.with().fullName(DontGenerate.class)).getFirstOrNull();
		if( dontGenerate != null){
			return false;
		}
		JAnnotation generated = annotations.find(AJAnnotation.with().fullName(IsGenerated.class)).getFirstOrNull();
		if(generated != null){
			Expression attributeExp = generated.getAttributeValueOrNull(PROP_BY);
			if(attributeExp instanceof StringLiteral){
				String generator = ((StringLiteral)attributeExp).getLiteralValue();
				return isGeneratorMatch(generator);
			} else if(attributeExp instanceof QualifiedName){ //lets see if the referenced generator points to a static field we can read the value of
				QualifiedName qn = (QualifiedName)attributeExp;
				String fullName = qn.getFullyQualifiedName();
				
				Boolean match = cachedFieldValueMatchResults.get(fullName); 
				if(match != null){
					return match;
				}
				String fieldValue = resolveFieldValue(annotations.getAstNode(), qn);
				match = isGeneratorMatch(fieldValue);
				cachedFieldValueMatchResults.put(fullName, match);
				return match;
			} else if(attributeExp instanceof FieldAccess){ //lets see if the referenced generator points to a static field we can read the value of
				FieldAccess fa = (FieldAccess)attributeExp;
				Expression exp = fa.getExpression();
				if(exp instanceof Name){
					Name name = (Name)exp;
					//e.g. field =   Foo.Bar
					//com.mycompany.Foo
					String className = NameUtil.resolveQualifiedName(name);
					//Bar
					String fieldName  = fa.getName().toString();
					String fullName = className + "." + fieldName;
					Boolean match = cachedFieldValueMatchResults.get(fullName); 
					if(match != null){
						return match;
					}
					String fieldValue = resolveFieldValue(annotations.getAstNode(), className, fieldName);
					match = isGeneratorMatch(fieldValue);
					cachedFieldValueMatchResults.put(fullName, match);
					return match;
				}
			}
			//String hash = anon.getValueForAttribute("sha1", null);
		}
		return false;
	}

	private String resolveFieldValue(ASTNode fromNode, QualifiedName qn) {
		String className = NameUtil.resolveQualifiedName(qn.getQualifier());
		String fieldName = qn.getName().toString();
		return resolveFieldValue(fromNode, className,fieldName);
	}
	
	private String resolveFieldValue(ASTNode fromNode,String className, String fieldName){
		String fieldValue = null;
		JType type = MutateUtil.getSourceLoaderOrFail(fromNode).loadTypeForClass(className);
		
		if(type!=null){
			JField field = type.findFieldsMatching(AJField.with().name(fieldName).isStatic()).getFirstOrNull();
			if(field != null){
				List<VariableDeclarationFragment> frags = field.getAstNode().fragments();
				if(frags.size() == 1){
					VariableDeclarationFragment val = frags.get(0);
					Expression varExp = val.getInitializer();
					if(varExp instanceof StringLiteral){
						fieldValue = ((StringLiteral)varExp).getLiteralValue();
					}
				}
			}
		}
		return fieldValue;
	}
	
	private static void log(String msg){
		log.debug(msg);
		//System.out.println(CodeGenMetaGenerator.class.getName() + " : " + msg);
	}

	private boolean isGeneratorMatch(String generator) {
		if(generator != null && (generator.equalsIgnoreCase(getFullConstantFieldPath()) || generator.contains(this.generator.getName()))){
			return true;
		}
		return false;
	}
	
	
    public void addGeneratedMarkers(SourceTemplate template) {
    	createClassIfNotExists();
        template.var("by", getFullConstantFieldPath());
        template.pl("@" + javax.annotation.Generated.class.getName() + "(${by})");
        template.pl("@" + IsGenerated.class.getName() + "(" + PROP_BY + "=${by})");
    }
    
    public void addGeneratedMarkers(BodyDeclaration type){
		addGeneratedMarkers(type.modifiers(),type.getAST());
    }
    
    private void addGeneratedMarkers(List<IExtendedModifier> modifiers,AST ast){
    	createClassIfNotExists();
    	
		NormalAnnotation a = ast.newNormalAnnotation();
		a.setTypeName(ast.newName(IsGenerated.class.getName()));
		QualifiedName gen = ast.newQualifiedName(ast.newName(CODE_GEN_INFO_CLASS_FULLNAME), ast.newSimpleName(getConstantFieldName()));
		addValue(a,PROP_BY,gen);
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
        	source = ctxt.getSourceLoader().loadSourceFrom(resource);
        } else {
        	log.debug("source " + resource.getRelPath() + " does not exist, creating" );
        	source = ctxt.newSourceTemplate()
                .var("pkg", CODE_GEN_INFO_CLASS_PKG)
                .var("className", CODE_GEN_INFO_CLASS_NAME)
                .pl("package ${pkg};")
                .pl("public class ${className} {}")
                .asSourceFileSnippet(root);
           //TODO:maybe track automatically via template create method?
           ctxt.getSourceLoader().trackChanges(source);
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
