package org.codemucker.jmutate.ast;

import static org.codemucker.lang.Check.checkNotNull;

import java.util.List;

import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.PlacementStrategies;
import org.codemucker.jmutate.transform.InsertCtorTransform;
import org.codemucker.jmutate.transform.InsertFieldTransform;
import org.codemucker.jmutate.transform.InsertMethodTransform;
import org.codemucker.jmutate.transform.InsertTypeTransform;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.ClassNameUtil;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class JTypeMutator {
	
	private final JType jType;
	private final JMutateContext ctxt;
	
	public JTypeMutator(JMutateContext context, AbstractTypeDeclaration type) {
		this(context, JType.from(type));
	}
	
	public JTypeMutator(JMutateContext context, JType javaType) {
		checkNotNull("javaType", javaType);
		this.jType = checkNotNull("type",javaType);
		this.ctxt = checkNotNull("context",context);
	}

	public JType getJType() {
    	return jType;
    }
	
	public void setAccess(JAccess access){
		jType.getModifiers().setAccess(access);
	}
	
	public JModifier getJavaModifiers(){
		return jType.getModifiers();
	}
	
	public ImportDeclaration newImport(String fqn){
		AST ast = jType.getAst();
		Name name = ast.newName(fqn);
		ImportDeclaration imprt = ast.newImportDeclaration();
		imprt.setName(name);
		return imprt;
	}
	
	public void addField(String src){
		FieldDeclaration field = newSourceTemplate()
			.setTemplate(src)
			.asResolvedFieldNode();
		addField(field);
	}

	public void addField(JField field){
	    addField(field.getAstNode());
	}
	
	public void addField(FieldDeclaration field){
		ctxt.obtain(InsertFieldTransform.class)
			.target(jType)
			.field(field)
			.placementStrategy(getStrategies().getFieldStrategy())
			.transform();
	}

	public void addMethod(String src){
		MethodDeclaration method = newSourceTemplate()
			.setTemplate(src)
			.asResolvedMethodNode();
		addMethod(method);
	}
    
	public void addMethod(JMethod method){
        addMethod(method.getAstNode());
    }
    
	public void addMethod(MethodDeclaration method){
		if( method.isConstructor()){
			//TODO:do we really want to check and change this? Should we throw an exception instead?
			//addCtor(method);
			throw new JMutateException("Trying to add a constructor as a method. Try adding it as a constructor instead. Ctor is " + method);
		}
		ctxt.obtain(InsertMethodTransform.class)
    		.target(jType)
    		.method(method)
    		.transform();
	}

	public void addCtor(String src){
		MethodDeclaration ctor = newSourceTemplate()
			.setTemplate(src)
			.asResolvedConstructorNode();
		addCtor(ctor);
	}
	
	public void addCtor(MethodDeclaration ctor){
		ctxt.obtain(InsertCtorTransform.class)
    		.target(jType)
    		.setCtor(ctor)
    		.placementStrategy(getStrategies().getCtorStrategy())
    		.transform();
	}
	
	public void addType(String src){
		AbstractTypeDeclaration type = newSourceTemplate()
			.setTemplate(src)
			.asResolvedTypeNodeNamed(null);
		addType(type);
	}
	
	public void addType(AbstractTypeDeclaration type){
		InsertTypeTransform.newTransform()
			.target(jType)
			.setType(type)
			.placementStrategy(getStrategies().getTypeStrategy())
			.transform();
	}
	/**
	 * Add an 'implements x' if this types doesn't already implement x
	 * 
	 * @param fullName
	 */
	public void addImplements(String fullName){
	    if(jType.isClass()){
	        TypeDeclaration type = jType.asTypeDecl();
	        List<Type> types = type.superInterfaceTypes();
	        for(Type t:types){
	            if(NameUtil.resolveQualifiedName(t).equals(fullName)){
	                return;//lets not add it more than once
	            }
	        }
	        AST ast = jType.getAst();
	        String pkgPart = ClassNameUtil.extractPkgPartOrNull(fullName);
	        String classPart = ClassNameUtil.extractSimpleClassNamePart(fullName);
	        Type newType;
            if (pkgPart != null) {
                newType = ast.newNameQualifiedType(ast.newName(pkgPart), ast.newSimpleName(classPart));
            } else {
                newType = ast.newSimpleType(ast.newSimpleName(classPart));
            }
            types.add(newType);
        }
    }
	
	public void addImport(String fullName){
		jType.getJCompilationUnit().addImport(fullName);

    }
	
	private SourceTemplate newSourceTemplate(){
		return ctxt.obtain(SourceTemplate.class);
	}
	
	private PlacementStrategies getStrategies(){
		return ctxt.obtain(PlacementStrategies.class);
	}
}