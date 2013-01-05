package com.bertvanbrakel.codemucker.ast;

import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.Root;
import com.google.common.base.Preconditions;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;

//TODO:remove existing entries if exists
public class JSearchIndexingVisitor extends JFindVisitor {
	
	static enum Mode {
		CREATE,UPDATE;
	}
	
	private final OGraphDatabase db;
	
	//Reuse docs for faster indexing
	private ODocument rootDoc = new ODocument(); 
	private ODocument resourceDoc = new ODocument(); 
	private ODocument sourceDoc = new ODocument(); 
	private ODocument typeDoc = new ODocument(); 
	private ODocument methodDoc = new ODocument(); 
	private ODocument fieldDoc = new ODocument(); 
	
	private Root currentRoot;
	private ClassPathResource currentResource;
	private JSourceFile currentSource;
	
	private int saveCount = 0;
	
	public JSearchIndexingVisitor(OGraphDatabase db){
		this(db,Mode.CREATE);
	}
	
	private JSearchIndexingVisitor(OGraphDatabase db, JSearchIndexingVisitor.Mode mode){
		Preconditions.checkNotNull(db,"db");
		this.db = db;
	}
	
	@Override
	public boolean visit(Root root) {
		currentRoot = root;
		
		rootDoc.reset();
		rootDoc.setClassName("Root");
		rootDoc.field("path", root.getPathName());

		return true;
	}

	@Override
	public void endVisit(Root root) {
		save(rootDoc, root.getPathName());
		currentRoot = null;
	}

	@Override
	public boolean visit(ClassPathResource resource) {
		currentResource = resource;
			
		resourceDoc.reset();
		resourceDoc.addOwner(rootDoc);
		resourceDoc.setClassName("Resource");
		resourceDoc.field("path", resource.getRelPath());
		
		return true;
	}

	@Override
	public void endVisit(ClassPathResource resource) {
		save(resourceDoc, resource.getRelPath());
		currentResource = null;
	}

	@Override
	public boolean visitClass(String className) {
		return true;
	}

	@Override
	public void endVisitClass(String className) {
	}

	@Override
	public boolean visit(JSourceFile source) {
		currentSource = source;
		
		sourceDoc.reset();
		sourceDoc.addOwner(resourceDoc);
		sourceDoc.setClassName("Source");
		sourceDoc.field("root", currentRoot.getPathName());	
		sourceDoc.field("resource", currentResource.getRelPath());
		sourceDoc.field("classname", source.getClassnameBasedOnPath());
	
		return true;
	}

	@Override
	public void endVisit(JSourceFile source) {
		save(sourceDoc, source.getClassnameBasedOnPath());
		currentSource = null;
	}
	
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		indexType(JType.from(node));
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		indexType(JType.from(node));
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		indexType(JType.from(node));
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		indexType(JType.from(node));
		return true;
	}

	//TODO:types,fields,methods
	private void indexType(JType type){
		typeDoc.reset();
		//typeDoc.addOwner(sourceDoc);
		typeDoc.setClassName("Type");
		
		typeDoc.field("fqdn", type.getFullName());
		typeDoc.field("resource",currentResource.getRelPath());
		typeDoc.field("source",currentSource.getClassnameBasedOnPath());
		
		typeDoc.field("simpleName", type.getSimpleName());
		typeDoc.field("package", type.getPackageName());
		
		typeDoc.field("isAbstract", type.isAbstract());
		typeDoc.field("isAnonymnous", type.isAnonymousClass());
		typeDoc.field("isAnnonation", type.isAnnotation());
		typeDoc.field("isConcrete", type.isConcreteClass());
		typeDoc.field("isEnum", type.isEnum());
		typeDoc.field("isInner", type.isInnerClass());
		typeDoc.field("isInterface", type.isInterface());
		typeDoc.field("isTop", type.isTopLevelClass());
		setModifiersFlags(typeDoc, type.getModifiers());

		save(typeDoc, type.getFullName());
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		JMethod method = JMethod.from(node);
		methodDoc.reset();
			
		//methodDoc.addOwner(typeDoc);
		methodDoc.setClassName("Method");
		//methodDoc.field("fqdn", type.getFullName());
		methodDoc.field("resource", currentResource.getRelPath());
		methodDoc.field("name", method.getName());
		methodDoc.field("numArgs", method.getParameters().size());
		methodDoc.field("isConstructor", method.isConstructor());
		methodDoc.field("isVoid", method.isVoid());
		
		//hasReturns
		//Simple patterns:
		//	isGetter
		//	isSetter			
		setModifiersFlags(methodDoc, method.getJavaModifiers());

		return true;
	}
	@Override
	public void endVisit(MethodDeclaration node) {
		save(methodDoc, node.getName().getIdentifier());
	}
	

	@Override
	public boolean visit(FieldDeclaration node) {
		JField field = JField.from(node);
		//fieldDoc.addOwner(typeDoc);
		fieldDoc.setClassName("Field");
		for(String name:field.getNames()){
			fieldDoc.field("name", name);
		}
		setModifiersFlags(fieldDoc, field.getJavaModifiers());

		save(fieldDoc, field.getName());
		
		return true;
	}

	private void save(ODocument doc, String id){
		saveCount++;
		System.out.println( saveCount +" saving " + doc.getClassName() + ":" + id);
		//TODO:add listener here for extension/
		db.save(doc);
	}
	
	private void setModifiersFlags(ODocument doc, JModifiers mods){
		doc.field("access", mods.asAccess().name());
		doc.field("isFinal", mods.isFinal());
		doc.field("isStatic", mods.isStatic());
		doc.field("isSynchronized", mods.isSynchronized());
	}
	
	
}