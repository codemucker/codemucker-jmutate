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
import com.bertvanbrakel.codemucker.transform.PlacementStrategies;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;

public class JTypeMutator {
	
	private final JType jType;
	private final MutationContext ctxt;
	
	public JTypeMutator(MutationContext context, AbstractTypeDeclaration type) {
		this(context, new JType(type));
	}
	
	public JTypeMutator(MutationContext context, JType javaType) {
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
		FieldDeclaration field = newSourceTemplate()
			.setTemplate(src)
			.asFieldNode();
		addField(field);
	}
	
	public void addField(FieldDeclaration field){
		ctxt.create(InsertFieldTransform.class)
			.setTarget(jType)
			.setField(field)
			.setPlacementStrategy(getStrategies().getFieldStrategy())
			.apply();
	}

	public void addMethod(String src){
		MethodDeclaration method = newSourceTemplate()
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
		ctxt.create(InsertMethodTransform.class)
    		.setTarget(jType)
    		.setMethod(method)
    		.setPlacementStrategy(getStrategies().getMethodStrategy())
    		.apply();
	}

	public void addCtor(String src){
		MethodDeclaration ctor = newSourceTemplate()
			.setTemplate(src)
			.asConstructor();
		addCtor(ctor);
	}
	
	public void addCtor(MethodDeclaration ctor){
		ctxt.create(InsertCtorTransform.class)
    		.setTarget(jType)
    		.setCtor(ctor)
    		.setPlacementStrategy(getStrategies().getCtorStrategy())
    		.apply();
	}
	
	public void addType(String src){
		AbstractTypeDeclaration type = newSourceTemplate()
			.setTemplate(src)
			.asType();
		addType(type);
	}
	
	public void addType(AbstractTypeDeclaration type){
		InsertTypeTransform.newTransform()
			.setTarget(jType)
			.setType(type)
			.setPlacementStrategy(getStrategies().getTypeStrategy())
			.apply();
	}
	
	private SourceTemplate newSourceTemplate(){
		return ctxt.create(SourceTemplate.class);
	}
	
	private PlacementStrategies getStrategies(){
		return ctxt.create(PlacementStrategies.class);
	}
}