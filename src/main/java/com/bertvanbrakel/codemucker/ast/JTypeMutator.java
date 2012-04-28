package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;

import com.bertvanbrakel.codemucker.transform.InsertCtorTransform;
import com.bertvanbrakel.codemucker.transform.InsertFieldTransform;
import com.bertvanbrakel.codemucker.transform.InsertMethodTransform;
import com.bertvanbrakel.codemucker.transform.InsertTypeTransform;
import com.bertvanbrakel.codemucker.transform.MutationContext;

public class JTypeMutator {
	
	private final JType jType;
	private final MutationContext context;
	
	public JTypeMutator(MutationContext context, AbstractTypeDeclaration type) {
		this(context, new JType(type));
	}
	
	public JTypeMutator(MutationContext context, JType javaType) {
		checkNotNull("javaType", javaType);
		this.jType = checkNotNull("type",javaType);
		this.context = checkNotNull("context",context);
	}

	public JType getJType() {
    	return jType;
    }
	
	public void setAccess(JAccess access){
		jType.getModifiers().setAccess(access);
	}
	
	public JModifiers getJavaModifiers(){
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
		FieldDeclaration field = context.newSourceTemplate()
			.setTemplate(src)
			.asFieldNode();
		addField(field);
	}
	
	public void addField(FieldDeclaration field){
		InsertFieldTransform.newTransform()
			.setTarget(jType)
			.setField(field)
			.setPlacementStrategy(context.getStrategies().getFieldStrategy())
			.apply();
	}

	public void addMethod(String src){
		MethodDeclaration method = context.newSourceTemplate()
			.setTemplate(src)
			.asMethodNode();
		addMethod(method);
	}
		
	public void addMethod(MethodDeclaration method){
		if( method.isConstructor()){
			//TODO:do we really want to check and change this? Should we throw an exception instead?
			//addCtor(method);
			throw new CodemuckerException("Trying to add a constructor as a method. Try adding it as a constructor instead. Ctor is " + method);
		}
		InsertMethodTransform.newTransform()
    		.setTarget(jType)
    		.setMethod(method)
    		.setPlacementStrategy(context.getStrategies().getMethodStrategy())
    		.apply();
	}

	public void addCtor(String src){
		MethodDeclaration ctor = context.newSourceTemplate()
			.setTemplate(src)
			.asConstructor();
		addCtor(ctor);
	}
	
	public void addCtor(MethodDeclaration ctor){
		InsertCtorTransform.newTransform()
    		.setTarget(jType)
    		.setCtor(ctor)
    		.setPlacementStrategy(context.getStrategies().getCtorStrategy())
    		.apply();
	}
	
	public void addType(String src){
		AbstractTypeDeclaration type = context.newSourceTemplate()
			.setTemplate(src)
			.asType();
		addType(type);
	}
	
	public void addType(AbstractTypeDeclaration type){
		InsertTypeTransform.newTransform()
			.setTarget(jType)
			.setType(type)
			.setPlacementStrategy(context.getStrategies().getTypeStrategy())
			.apply();
	}

}