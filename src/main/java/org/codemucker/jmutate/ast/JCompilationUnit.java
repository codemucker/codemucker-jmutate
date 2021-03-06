package org.codemucker.jmutate.ast;

import static org.codemucker.lang.Check.checkNotNull;

import java.util.Collection;
import java.util.List;

import org.codemucker.jfind.DefaultFindResult;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.IProvideCompilationUnit;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ResourceLoader;
import org.codemucker.jmutate.SourceLoader;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.util.MutateUtil;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import com.google.common.collect.Lists;

public class JCompilationUnit implements AstNodeProvider<CompilationUnit>, IProvideCompilationUnit {

	private final CompilationUnit compilationUnit;
	
	protected JCompilationUnit(CompilationUnit cu) {
		checkNotNull("compilationUnit", cu);
		this.compilationUnit = cu;
	}
	
	public static boolean is(ASTNode node){
        return node instanceof CompilationUnit;
    }
	
	public static JCompilationUnit findCompilationUnit(ASTNode node){
		return from(findCompilationUnitNode(node));
	}
	
	public static CompilationUnit findCompilationUnitNode(ASTNode node){
		ASTNode parent = node;
		while( parent != null){
			if( parent instanceof CompilationUnit){
				return (CompilationUnit)parent;
			}
			parent = parent.getParent();			
		}
		throw new JMutateException("Couldn't find compilation unit. Unexpected");
	}
	
	public static JCompilationUnit from(ASTNode node){
	    if(!is(node)){
	        throw new IllegalArgumentException(String.format("Expected a %s but was %s",
                JCompilationUnit.class.getName(),
                node.getClass().getName()
            ));
	    }
        return from((CompilationUnit)node);
    }
	
	public static JCompilationUnit from(CompilationUnit cu){
		return new JCompilationUnit(cu);
	}
	
	public RootResource getResource(){
        return getSource().getResource();
    }
	
	public JSourceFile  getSource(){
        return MutateUtil.getSource(compilationUnit);
    }
	
	public String getFullPackageName(){
		PackageDeclaration pkg = compilationUnit.getPackage();
		if( pkg == null){
			return null;
		}
		return pkg.getName().getFullyQualifiedName();
	}
	
	public JType findMainType() {
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> topTypes = compilationUnit.types();
		JType mainType = null;
		if(topTypes.isEmpty()){
			throw new JMutateException("no types found in compilation unit so couldn't determine source path to generate");
		} else if(topTypes.size() == 1){
			mainType = JType.from(topTypes.get(0));
		} else { //find the public class
			for(AbstractTypeDeclaration node:topTypes){
				JType type = JType.from(node);
				if(type.getModifiers().isPublic()){
					if( mainType != null){
						throw new JMutateException("Multiple top level types in compilation unit and more than one is public. Can't determine main type");
					}
					mainType = type;
				}
			}
			if(mainType == null){
				throw new JMutateException("Multiple top level types in compilation unit and could not determine one to use. Try setting one to public access");
			}
		}
		return mainType;
	}
	
	public FindResult<JType> findAllTypes(){
		return findTypesMatching(AJType.any());
	}
	
	public FindResult<JType> findTypesMatching(final Matcher<JType> matcher){
		final Collection<JType> found = Lists.newArrayList();
		ASTVisitor visitor = new BaseASTVisitor() {
			@Override
			protected boolean visitNode(ASTNode node) {
				if(JType.isTypeNode(node)){
					JType type = JType.from(node);
					if(matcher.matches(type)) {
						found.add(type);
					}
				}
				return true;
			}
		};
		visitChildren(visitor);
		return DefaultFindResult.from(found);
	}
	
	private void visitChildren(final ASTVisitor visitor){
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> topTypes = compilationUnit.types();
		
		for(AbstractTypeDeclaration topType:topTypes){
			topType.accept(visitor);
		}
	}

	@Override
	public CompilationUnit getAstNode() {
		return compilationUnit;
	}

	/**
	 * Ad a new import if it doesn't already exist
	 * 
	 * TODO:support wildcard import
	 */
	public void addImport(String fullName){
        List<ImportDeclaration> imports = compilationUnit.imports();
        for(ImportDeclaration dec:imports){
            if(fullName.equals(dec.getName().getFullyQualifiedName())){
                    return;
            }
        }
        AST ast = compilationUnit.getAST();
        ImportDeclaration newImport = ast.newImportDeclaration();
        newImport.setName(ast.newName(fullName));
        imports.add(newImport);
    }

	@Override
	public JCompilationUnit getCompilationUnit() {
		return this;
	}
	
	public SourceLoader getSourceLoader(){
		return MutateUtil.getSourceLoaderOrFail(compilationUnit);
	}
}
