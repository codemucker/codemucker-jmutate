package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.collect.Sets.newTreeSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.AstNodeProvider;
import org.codemucker.jmutate.ast.BaseASTVisitor;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.ClassNameUtil;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.internal.core.Member;

import com.google.inject.Inject;

/**
 * Converts types in a class to short names and adds an import. Useful when generating code to use fully qualified
 * names in code modifications, then apply this after to insert correct imports,
 */
public class CleanImportsTransform implements Transform {

    private static final Logger log = LogManager.getLogger(CleanImportsTransform.class);
    
	@Inject
	private JMutateContext ctxt;
	
	private ASTNode node;
	
	//TODO:add options to restrict what get cleaned in case the cleaner is not doing the right thing
	//ignore imports
	//importsToFix - if not null only these will be fixed, no search will be performed. 

	private boolean addMissingImports = true;
	
	@Override
	public void transform() {
		checkNotNull(ctxt, "expect ctxt");
		checkNotNull(node, "expect node");
		
		//find existing imports
		CompilationUnit cu = findCompilationUnit();
		List<ImportDeclaration> imports = cu.imports();		
		
		Map<String,String> classNamesByShortNameToConvert = toClassNamesByShortName(imports);
		
		if(addMissingImports){
			//find additiona l imports to add and then convert
			
			Map<String,String> importsToAdd = toFullNamesKeyedByShortName(findAllClassNamesIn(node));
			importsToAdd.keySet().removeAll(toShortNames(imports));
			
			//merge the above two results into a set which contains all the long name to short name mappings to apply
			classNamesByShortNameToConvert.putAll(importsToAdd);
			
			//add the additional import declarations
			AST ast = cu.getAST();
			Set<String> sortedImportsToAdd = new TreeSet<>(importsToAdd.values());
			for(String className:sortedImportsToAdd){
				if( !isIgnoreImport(className)){
					ImportDeclaration importDec = createImport(ast,className);
					cu.imports().add(importDec);
				}
			}
		} else {
			//always clean up use java types which are available without an import
			Map<String,String> classNames = toFullNamesKeyedByShortName(findAllClassNamesIn(node));
			for(Map.Entry<String, String> entry:classNames.entrySet()){
				String className = entry.getValue();
				if( isBuiltInType(className)){
					classNamesByShortNameToConvert.put(entry.getKey(), entry.getValue());
				}
			}
		}
		
		//shorten all fqn's in imports list
		FullNameShortenerVisitor shortenerVisitor = new FullNameShortenerVisitor(swapKeysAndValues(classNamesByShortNameToConvert));
		node.accept(shortenerVisitor);
		
	}
	
	//TODO:list of exclusions, configurable...
	private boolean isIgnoreImport(String fullName){
		return isBuiltInType(fullName);
	}
	
	private boolean isBuiltInType(String fqdn){
		return fqdn.startsWith("java.lang.");
	}
	
	private static <K,V> Map<V,K> swapKeysAndValues(Map<K,V> map){
		Map<V, K> swapped = newHashMapWithExpectedSize(map.size());
		for(Entry<K, V> e:map.entrySet()){
			//TODO:bail if clashes?
			swapped.put(e.getValue(), e.getKey());
		}
		return swapped;
	}
	
	private static List<String> findAllClassNamesIn(ASTNode node){
		QualifiedNameCollectorVisitor visitor = new QualifiedNameCollectorVisitor();
		node.accept(visitor);
		List<String> fqdns = newArrayList(visitor.foundClassNames);
		return fqdns;
	}
	
	private static ImportDeclaration createImport(AST ast, String fqn){
		Name name = ast.newName(fqn);
		ImportDeclaration dec = ast.newImportDeclaration();
		dec.setName(name);
		return dec;
	}
	
	private static Collection<String> toShortNames(Collection<ImportDeclaration> imports){
		return toClassNamesByShortName(imports).keySet();
	}

	private static Map<String,String> toClassNamesByShortName(Collection<ImportDeclaration> imports){
		Map<String,String> map = newHashMap();
		for(ImportDeclaration dec:imports){
		    if(!dec.isStatic() && dec.getName().isQualifiedName()){
		        QualifiedName qn = (QualifiedName) dec.getName();
		        map.put(qn.getName().getIdentifier(),qn.getFullyQualifiedName());   
		    }
		}
		return map;
	}

	private static Map<String,String> toFullNamesKeyedByShortName(Collection<String> qualifiedNames){
		Map<String,String> map = newHashMap();
		for(String longName:qualifiedNames){
			//TODO:detect clash and choose the one with the greatest number of occurrences
			//for now first one wins..
			String shortName = ClassNameUtil.extractSimpleClassNamePart(longName);
			if( !map.containsKey(shortName)){
				map.put(shortName, longName);		
			}
		}
		return map;
	}

	public CleanImportsTransform nodeToClean(AstNodeProvider<?> provider) {
		nodeToClean(provider.getAstNode());
		return this;
	}

	public CleanImportsTransform nodeToClean(ASTNode node) {
		this.node = node;
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
	
	private static class FullNameShortenerVisitor extends BaseASTVisitor {
		private static Logger log = Logger.getLogger(FullNameShortenerVisitor.class);
			
		private Map<String,String> shortNamesByFqnsToConvert = newHashMap();
		
		private int depth = 0;
		
		FullNameShortenerVisitor(Map<String,String> shortNamesByFqnsToConvert){
			this.shortNamesByFqnsToConvert = newHashMap(shortNamesByFqnsToConvert);
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
		public boolean visit(QualifiedName node) {
			if( depth > 0){
				return false;
			}
			//convert qualified name to simple name
			String shortName = shortNamesByFqnsToConvert.get(node.getFullyQualifiedName());
		
			if( shortName != null){
                switch (node.getParent().getNodeType()) {
                case ASTNode.SIMPLE_TYPE: {
                    SimpleType parent = (SimpleType) node.getParent();
                    parent.setName(node.getAST().newSimpleName(shortName));
                }
                    break;
//                case ASTNode.NORMAL_ANNOTATION:
//                case ASTNode.MARKER_ANNOTATION:
//                case ASTNode.SINGLE_MEMBER_ANNOTATION: {
//                    Annotation parent = (Annotation) node.getParent();
//                    parent.setTypeName(node.getAST().newSimpleName(shortName));
//                }
//                    break;
                case ASTNode.QUALIFIED_NAME: {
                    QualifiedName parent = (QualifiedName) node.getParent();
                    parent.setQualifier(node.getAST().newSimpleName(shortName));
                }
                    break;
                case ASTNode.METHOD_INVOCATION: {
                    MethodInvocation parent = (MethodInvocation) node.getParent();
                    parent.setExpression(node.getAST().newSimpleName(shortName));
                }
                    break;
                default:
                    log("unknown parent type to set simple name on:" + node.getParent().getClass().getName());
                }
			}
			return super.visit(node);
		}
		
	
		@Override
		public void endVisit(QualifiedName node) {
			if( depth > 0){
				depth--;
			}
			super.endVisit(node);
		}
		
		private void log(String msg){
			log.debug( "depth:" + depth + ",msg:" + msg);
		}
		
	}
	
	//collect all the fully qualified names
	private static class QualifiedNameCollectorVisitor extends BaseASTVisitor {

		private Set<String> foundClassNames = newTreeSet();

		private int depth = 0;

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
        public boolean visit(QualifiedType node) {
            if( depth > 0){
                return false;
            }
            String fullName = NameUtil.resolveQualifiedNameOrNull(node);
            if(fullName != null){
                addFullName(fullName);
            }
            depth++;
            return false;
        }
        
        @Override
        public boolean visit(NameQualifiedType node) {
            if( depth > 0){
                return false;
            }
            addFullName(node.getQualifier().getFullyQualifiedName() + "." + node.getName().getIdentifier());
            depth++;
            return false;
        }
        
		@Override
		public boolean visit(QualifiedName node) {
			if( depth > 0){
				return false;
			}
			//don't handle fq field names yet
			if(node.getParent() instanceof MemberValuePair || node.getParent() instanceof Expression){
			    return false;
			}
			addFullName(node.getFullyQualifiedName());
            depth++;
			return super.visit(node);
		}
		
		private void addFullName(String fullName){
		    foundClassNames.add(fullName);
		}
		
		@Override
		public void endVisit(QualifiedName node) {
			if( depth > 0){
				depth--;
			}
			super.endVisit(node);
		}
		
		@Override
        public void endVisit(NameQualifiedType node) {
            if( depth > 0){
                depth--;
            }
            super.endVisit(node);
        }   
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
}
