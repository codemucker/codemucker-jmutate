package com.bertvanbrakel.codemucker.ast;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;

import com.bertvanbrakel.codemucker.transform.CodeMuckContext;
import com.bertvanbrakel.codemucker.transform.InsertCtorTransform;
import com.bertvanbrakel.codemucker.transform.InsertFieldTransform;
import com.bertvanbrakel.codemucker.transform.InsertMethodTransform;
import com.bertvanbrakel.codemucker.transform.InsertTypeTransform;
import com.bertvanbrakel.codemucker.transform.PlacementStrategies;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;

public class JTypeMutator {
	
	private final JType jType;
	private final CodeMuckContext ctxt;
	
	public JTypeMutator(CodeMuckContext context, AbstractTypeDeclaration type) {
		this(context, JType.from(type));
	}
	
	public JTypeMutator(CodeMuckContext context, JType javaType) {
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
			.asResolvedFieldNode();
		addField(field);
	}
	
	public void addField(FieldDeclaration field){
		ctxt.obtain(InsertFieldTransform.class)
			.setTarget(jType)
			.setField(field)
			.setPlacementStrategy(getStrategies().getFieldStrategy())
			.transform();
	}

	public void addMethod(String src){
		MethodDeclaration method = newSourceTemplate()
			.setTemplate(src)
			.asResolvedMethodNode();
		addMethod(method);
	}
		
	public void addMethod(MethodDeclaration method){
		if( method.isConstructor()){
			//TODO:do we really want to check and change this? Should we throw an exception instead?
			//addCtor(method);
			throw new CodemuckerException("Trying to add a constructor as a method. Try adding it as a constructor instead. Ctor is " + method);
		}
		ctxt.obtain(InsertMethodTransform.class)
    		.setTarget(jType)
    		.setMethod(method)
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
    		.setTarget(jType)
    		.setCtor(ctor)
    		.setPlacementStrategy(getStrategies().getCtorStrategy())
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
			.setTarget(jType)
			.setType(type)
			.setPlacementStrategy(getStrategies().getTypeStrategy())
			.transform();
	}
	
	private SourceTemplate newSourceTemplate(){
		return ctxt.obtain(SourceTemplate.class);
	}
	
	private PlacementStrategies getStrategies(){
		return ctxt.obtain(PlacementStrategies.class);
	}
}