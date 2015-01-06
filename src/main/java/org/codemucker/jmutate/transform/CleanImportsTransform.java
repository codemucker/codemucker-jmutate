package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ResourceLoader;
import org.codemucker.jmutate.ast.AstNodeProvider;
import org.codemucker.jmutate.ast.BaseASTVisitor;
import org.codemucker.jmutate.ast.JCompilationUnit;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.ClassNameUtil;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Converts types in a class to short names and adds an import. Useful when generating code to use fully qualified
 * names in code modifications, then apply this after to insert correct imports,
 */
public class CleanImportsTransform implements Transform {

    private static final Logger log = LogManager.getLogger(CleanImportsTransform.class);
    
    private static final boolean SYSOUT_DEBUG = false;
    
	@Inject
	private JMutateContext ctxt;
	
	private ASTNode node;

	//options to restrict what get cleaned in case the cleaner is not doing the right thing
	private Set<String> ignoreShortNames = new HashSet<>();
	private Set<String> ignoreFullNames = new HashSet<>();
	
	//optionally ignore imports
	private boolean addMissingImports = true;
	
	@Override
	public void transform() {
		checkNotNull(ctxt, "expect ctxt");
		checkNotNull(node, "expect node");

		
		//find existing imports
		CompilationUnit cuNode = findCompilationUnit();
		Replacements replacements = new Replacements(ctxt.getResourceLoader(), cuNode);
		replacements.addIgnoreShortNames(ignoreShortNames);
		replacements.addIgnoreFullNames(ignoreFullNames);
		replacements.addImports(cuNode.imports());
		
		JCompilationUnit cu = JCompilationUnit.from(cuNode);
		
		//lets just ignore all clashes with inner types for now. Likely just be confusing to not use full names
		for(JType type:cu.findAllTypes()){
			replacements.addIgnoreFullName(type.getFullName());
			replacements.addIgnoreShortName(type.getSimpleName());
		}
		
		if(addMissingImports){		
			// find additional imports to add and then convert
			node.accept(new QualifiedNameCollectorVisitor(ctxt,replacements){
				@Override
				protected boolean doAdd(Replacement replacement) {
					return isBuiltInType(replacement.qualifiedName) || !isIgnoreImport(replacement.qualifiedName);
				}
			});
		} else {
			// always clean up used java types which are available without an
			// import (i.e. java.lang.*)
			node.accept(new QualifiedNameCollectorVisitor(ctxt,replacements){
				@Override
				protected boolean doAdd(Replacement replacement) {
					return isBuiltInType(replacement.qualifiedName);
				}
			});			
		}
		addMissingImports(cuNode, replacements);

		//shorten all fqn's in imports list
		node.accept(new QualifiedNameShortenerVisitor(replacements));
	}

	private void addMissingImports(CompilationUnit cu, Replacements replacements) {
		List<ImportDeclaration> imports = cu.imports();		
		Set<String> importsFullNames = new TreeSet<>();
		for(ImportDeclaration dec:imports){
			if(!dec.isStatic() && dec.getName().isQualifiedName()){
		        QualifiedName qn = (QualifiedName) dec.getName();
		        importsFullNames.add(qn.getFullyQualifiedName());   
		    }
		}
		Set<String> importsToAdd = new TreeSet<>();
		
		for(Replacement node:replacements.getReplacements()){
			if(!importsFullNames.contains(node.importName)){
				importsToAdd.add(node.importName);
			}
		}
		
		AST ast = cu.getAST();
		for(String imprt:importsToAdd){
			if(!isBuiltInType(imprt)){
				cu.imports().add(createImport(ast, imprt));
			}
		}
	}
		
	private boolean isIgnoreImport(String fullName){
		return isBuiltInType(fullName);
	}
	
	private boolean isBuiltInType(String fqdn){
		return fqdn.startsWith("java.lang.");
	}
	
	private static ImportDeclaration createImport(AST ast, String fqn){
		Name name = ast.newName(fqn);
		ImportDeclaration dec = ast.newImportDeclaration();
		dec.setName(name);
		return dec;
	}

	public CleanImportsTransform nodeToClean(AstNodeProvider<?> provider) {
		nodeToClean(provider.getAstNode());
		return this;
	}

	public CleanImportsTransform nodeToClean(ASTNode node) {
		this.node = node;
		return this;
	}
	
	public CleanImportsTransform ignoreName(String shortOrFullName) {
		if(shortOrFullName.contains(".")){
			ignoreFullNames.add(shortOrFullName);
		} else {
			ignoreShortNames.add(shortOrFullName);
		}
		return this;
	}

	public CleanImportsTransform ctxt(JMutateContext ctxt) {
		this.ctxt = ctxt;
		return this;
	}
	
	public CleanImportsTransform addMissingImports(boolean b) {
		this.addMissingImports = b;
		return this;
	}

	private CompilationUnit findCompilationUnit() {
		ASTNode parent = node;
		while (parent != null) {
			if (parent instanceof CompilationUnit) {
				return (CompilationUnit) parent;
			}
			parent = parent.getParent();
		}
		throw new JMutateException("Couldn't find compilation unit");
	}
	
	private static class Replacements {
		
		private final JCompilationUnit cu;
		private final String pkgPrefix;
		private final ResourceLoader resourceLoader;
		
		public Replacements(ResourceLoader loader, CompilationUnit cuNode) {
			super();
			this.cu = JCompilationUnit.from(cuNode);
			String pkg = cu.getFullPackageName();
			this.pkgPrefix = pkg==null?"":(pkg + ".");
			this.resourceLoader = loader;
		}

		private List<String> ignoreShortNames = new ArrayList<>();
		private List<String> ignoreFullNames = new ArrayList<>();
		
		private Map<String,Replacement> byImport = new HashMap<>();
		private Map<String,Replacement> byQualifiedName = new HashMap<>();
		private Map<String,Replacement> byShortName = new HashMap<>();
		
		public void addIgnoreShortNames(Collection<String> names){
			ignoreShortNames.addAll(names);
		}
		
		public void addIgnoreFullNames(Collection<String> names){
			ignoreFullNames.addAll(names);
		}
		
		public void addIgnoreShortName(String shortName){
			ignoreShortNames.add(shortName);
		}
		
		public void addIgnoreFullName(String fullName){
			ignoreFullNames.add(fullName);
		}
		
		public void addImports(Collection<ImportDeclaration> imports){
			for(ImportDeclaration dec:imports){
			    if(!dec.isStatic() && dec.getName().isQualifiedName()){
			        QualifiedName qn = (QualifiedName) dec.getName();
			        add(new Replacement(qn.getFullyQualifiedName()));
			    }
			}
		}
		
		public boolean add(Replacement replacement){
			if(!ignoreReplacement(replacement) && !clashingReplacementExists(replacement) && !clashingLocalTypeExists(replacement)){
				byImport.put(replacement.importName,replacement);
				byQualifiedName.put(replacement.qualifiedName,replacement);
				byShortName.put(replacement.shortName,replacement);
				
				log("" + replacement.qualifiedName + " --shorten-to--> " + replacement.shortName + " --import--> " + replacement.importName);
				
				return true;
			}
			return false;
		}
		
		private boolean clashingLocalTypeExists(Replacement replacement){
			String shortClassName = ClassNameUtil.extractSimpleClassNamePart(replacement.importName);
			
			String localName = pkgPrefix + shortClassName;
			log("clash:localname=" + localName + ",fullname=" + replacement.qualifiedName+ ",import=" + replacement.importName);
			if(localName.equals(replacement.importName)){
				//resolves to the same, so ok
				return false;
			}
			
			if(resourceLoader.canLoadClassOrSource(localName)){
				
				//local name the same and doesn't resolve to the same qualified type
				addIgnoreShortName(shortClassName);
				addIgnoreFullName(replacement.importName);
				
				return true;
			}
			//no local type
			return false;
		}
		
		private boolean clashingReplacementExists(Replacement replacement){
			return byQualifiedName.containsKey(replacement.qualifiedName) || byShortName.containsKey(replacement.shortName);
		}
		
		private boolean ignoreReplacement(Replacement replacement){
			String shortClassName = ClassNameUtil.extractSimpleClassNamePart(replacement.importName);
			
			return ignoreFullNames.contains(replacement.importName) || ignoreShortNames.contains(replacement.shortName)|| ignoreShortNames.contains(shortClassName);
		}
		
		public Replacement getByFullNameOrNull(String fullName){
			if(fullName == null){
				return null;
			}
			return byQualifiedName.get(fullName);
		}
		
		public List<Replacement> getReplacements(){
			return Lists.newArrayList(byImport.values());
		}
		
		private void log(String msg){
			if(SYSOUT_DEBUG){
				System.out.println(getClass().getSimpleName() + ":" + msg);
			}
			log.debug(msg);
		}
	}
	
	private static class Replacement {
		public final String qualifiedName;
		public final String importName;
		public final String shortName;
		
		public Replacement(String qualifiedName){
			this(qualifiedName,qualifiedName,ClassNameUtil.extractSimpleClassNamePart(qualifiedName));
		}
		
		public Replacement(String qualifiedName, String importName,	String replaceName) {
			super();
			this.qualifiedName = qualifiedName;
			this.importName = importName;
			this.shortName = replaceName;
		}	
	}
	

	/**
	 * collects all the fully qualified names to replace
	 */
	private static class QualifiedNameCollectorVisitor extends BaseASTVisitor {

		private final JMutateContext ctxt;
		private final Replacements replacements;

		private QualifiedNameCollectorVisitor(JMutateContext ctxt,Replacements replacements){
			this.ctxt = ctxt;
			this.replacements = replacements;
		}
			
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
        public boolean visit(ArrayType node) {
        	Type type ;
        	if(node.getAST().apiLevel() < AST.JLS8){
        		type = node.getComponentType();
        	} else {
        		type = node.getElementType();
        	}
        	
        	String fullName = NameUtil.resolveQualifiedName(type);
    		if( fullName!= null){
    			collectFullName(fullName);
    		}
        	
        	return super.visit(node);
        }
        
        @Override
        public boolean visit(VariableDeclarationFragment node) {
        	Expression expression = node.getInitializer();
        	collectExpression(expression);
        	return super.visit(node);
        }

        @Override
        public boolean visit(SimpleType node) {
            String fullName = NameUtil.resolveQualifiedNameOrNull(node);
            if(fullName != null){
                collectFullName(fullName);
            }
            return false;
        }
        
        @Override
        public boolean visit(QualifiedType node) {
            String fullName = NameUtil.resolveQualifiedNameOrNull(node);
            if(fullName != null){
                collectFullName(fullName);
            }
            return false;
        }
        
        @Override
        public boolean visit(NameQualifiedType node) {
            collectFullName(node.getQualifier().getFullyQualifiedName() + "." + node.getName().getIdentifier());
            return false;
        }
        
        @Override
        public boolean visit(MemberValuePair node) {
        	collectExpression(node.getValue());
        	return super.visit(node);
        }
        
        @Override
		public boolean visit(SingleMemberAnnotation node) {
        	collectName(node.getTypeName());
        	return super.visit(node);
        }
        
        @Override
		public boolean visit(NormalAnnotation node) {
        	collectName(node.getTypeName());
        	return super.visit(node);
        }
        
        @Override
		public boolean visit(MarkerAnnotation node) {
        	collectName(node.getTypeName());
        	return super.visit(node);
        }
        
        @Override
        public boolean visit(FieldAccess node) {
			if(node.getExpression() instanceof QualifiedName){
				QualifiedName qn = (QualifiedName)node.getExpression();
				collectFullName(qn.getFullyQualifiedName());
			}
        	return super.visit(node);
        }
        
        @Override
		public boolean visit(TypeLiteral node) {
			String fullName = NameUtil.resolveQualifiedNameOrNull(node.getType());
			if(fullName != null){
				collectFullName(fullName);
			}
			return super.visit(node);
		}
		
        private void collectName(Name name) {
        	if(name instanceof QualifiedName){
        		collectFullName(name.getFullyQualifiedName());
        	}
        }
        
		private void collectExpression(Expression expression) {
			if(expression instanceof QualifiedName){
				//could be a type expression or a field access
				QualifiedName qn = (QualifiedName)expression;
				String fullName = qn.getFullyQualifiedName();//com.foo.Bar.FOO
				//com.foo.Bar || com.foo.Bar.FOO
				if(isExistingClass(fullName)){ //com.foo.Bar
					collectFullName(fullName);
				} else { //com.foo.Bar.FOO
					String importName = qn.getQualifier().getFullyQualifiedName();//com.foo.Bar
					if (isExistingClass(importName)) {
						String shortName = ClassNameUtil.extractSimpleClassNamePart(qn.getQualifier().getFullyQualifiedName()) + "." + qn.getName();//Bar.FOO
						Replacement replaceWith = new Replacement(fullName,importName, shortName);
						collectReplacement(replaceWith);
					}
				}
			}
		}
        
        private boolean isExistingClass(String fullName){
			ResourceLoader loader = ctxt.getResourceLoader();
			return loader.canLoadClassOrSource(fullName);
        }

		private void collectFullName(String fullName) {
			if(fullName.indexOf('.') != -1){//ignore primitives
				collectReplacement(new Replacement(stripGenerics(fullName)));
			}
		}
		
		private static String stripGenerics(String fullName) {
			int idx = fullName.indexOf('<');
			if (idx != -1) {
				return fullName.substring(0, idx);
			}
			return fullName;
		}
		
		private void collectReplacement(Replacement replacement) {
			if (doAdd(replacement)) {
				replacements.add(replacement);
			}
		}
		
		/**
		 * Override me to filter replacements
		 * @param replacement
		 * @return true if the replacement should be made
		 */
		protected boolean doAdd(Replacement replacement){
			return true;
		}
		
		private void log(String msg){
			if(SYSOUT_DEBUG){
				System.out.println(getClass().getSimpleName() + ":" + msg);
			}
			log.debug(msg);
		}
		
	}
	
	/**
	 * Performs the actual replacement of full to short names where it can
	 */
	private static class QualifiedNameShortenerVisitor extends BaseASTVisitor {
		private static Logger log = Logger.getLogger(QualifiedNameShortenerVisitor.class);
	
		private Replacements replacements;
		
		QualifiedNameShortenerVisitor(Replacements replacements){
			this.replacements = replacements;
		}

		@Override
		public boolean visit(ImportDeclaration node) {
		    return false;
		}
		
		@Override
        public boolean visit(PackageDeclaration node) {
            return false;
        }

		@Override
        public boolean visit(ArrayType node) {
        	if(node.getAST().apiLevel() < AST.JLS8){
        		Type newType = newShortTypeOrNull(node.getComponentType());
    			if(newType != null){
    				node.setComponentType(newType);
    			}
        	} else {
        		Type newType = newShortTypeOrNull(node.getElementType());
    			if(newType != null){
    				node.setElementType(newType);
    			}
        	}
        	return super.visit(node);
        }
        
		
		@Override
		public boolean visit(FieldAccess node) {
			Expression newExpression = newShortExpessionOrNull(node.getExpression());
			if(newExpression != null){
				node.setExpression(newExpression);
			}
			return super.visit(node);
		}
		
		@Override
		public boolean visit(TypeLiteral node) {
			Type newType = newShortTypeOrNull(node.getType());
			if(newType != null){
				node.setType(newType);
			}
			return super.visit(node);
		}
		
		@Override
		public boolean visit(NormalAnnotation node){
			updateTypeName(node);
			return super.visit(node);
		}
		
		@Override
		public boolean visit(MarkerAnnotation node){
			updateTypeName(node);
			return super.visit(node);
		}
		
		@Override
		public boolean visit(SingleMemberAnnotation node){
			updateTypeName(node);
			Expression newExpression = newShortExpessionOrNull(node.getValue());
			if(newExpression != null){
				node.setValue(newExpression);
			}
			return super.visit(node);
		}
		
		private void updateTypeName(Annotation node){
			Name newName = newShortNameOrNull(node.getTypeName());
			if(newName != null){
				node.setTypeName(newName);
			}
		}
		
		@Override
		public boolean visit(MemberValuePair node){
			Expression newExpression = newShortExpessionOrNull(node.getValue());
			if(newExpression != null){
				node.setValue(newExpression);
			}
			
			return super.visit(node);
		}
		
		@Override
		public boolean visit(VariableDeclarationFragment node) {
			Expression newExpression = newShortExpessionOrNull(node.getInitializer());
			if(newExpression != null){
				node.setInitializer(newExpression);
			}
			return super.visit(node);
		}
		
		@Override
		public boolean visit(MethodInvocation node) {
			Expression newExpression = newShortExpessionOrNull(node.getExpression());
			if(newExpression != null){
				node.setExpression(newExpression);
			}
			return super.visit(node);
		}
		
		@Override
		public boolean visit(SimpleType node) {
			Name newName = newShortNameOrNull(node.getName());
			if(newName != null){
				node.setName(newName);
			}
			return super.visit(node);
		}

		private Expression newShortExpessionOrNull(Expression expression){
			if(expression instanceof QualifiedName){
				Name newName = newShortNameOrNull((Name)expression);
				if(newName != null){
					return newName;
				}
			} else if (expression instanceof FieldAccess){
				Name newName = newShortNameOrNull((FieldAccess)expression);
				if(newName != null){
					return newName;
				}
			} else if (expression instanceof TypeLiteral){
				TypeLiteral tl = (TypeLiteral)expression;
				Type newType = newShortTypeOrNull(tl.getType());
				if(newType != null){
					tl.setType(newType);
				}
			}
			return null;
		}
		
		private Replacement getReplacementOrNull(String fullname){
			Replacement replacement = replacements.getByFullNameOrNull(fullname);
			return replacement;
		}
		
		private Type newShortTypeOrNull(Type type){
			String fullName = NameUtil.resolveQualifiedNameOrNull(type);
			Replacement replace = getReplacementOrNull(fullName);
			if(replace != null){
				AST ast = type.getAST();
				return ast.newSimpleType(ast.newName(replace.shortName));	
			}
			return null;
		}
		
		private Name newShortNameOrNull(Name name){
			if(name instanceof QualifiedName){
				Replacement replacement = replacements.getByFullNameOrNull(name.getFullyQualifiedName());
				if(replacement != null){
					return name.getAST().newName(replacement.shortName);
				}
			}
			return null;
		}
		
		private Name newShortNameOrNull(FieldAccess fa){
			if(fa.getExpression() instanceof Name){
				Name newName = newShortNameOrNull((Name) fa.getExpression(),fa.getName());
				return newName;
			}
			return null;
		}
		
		private Name newShortNameOrNull(Name qualifier, Name name){
			String fullName = qualifier.getFullyQualifiedName() + "." + name.getFullyQualifiedName();
			Replacement replacement = replacements.getByFullNameOrNull(fullName);
			if(replacement != null){
				return name.getAST().newName(replacement.shortName);
			}
		
			return null;
		}

		private void log(String msg){
			if(SYSOUT_DEBUG){
				System.out.println(getClass().getSimpleName() + ":" + msg);
			}
			log.debug(msg);
		}
		
	}

}
