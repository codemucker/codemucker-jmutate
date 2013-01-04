package com.bertvanbrakel.codemucker.ast;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.test.finder.Root;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.query.nativ.ONativeQuery;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class JSearchIndex implements Closeable {

	private final OGraphDatabase db;
	private final IndexListener listener = new NullIndexListener();
	
	public JSearchIndex(List<Root> roots){
        db = Orient.instance()
        	.getDatabaseFactory()
        	.createGraphDatabase("local:/tmp/orientdb_codemucker_search_test" + System.currentTimeMillis());
        
        db.create();
       // db.begin();
        
        FindResult<JSourceFile> sources = JSourceFinder.newBuilder()
        	.setSearchRoots(roots)
        	.build()
        	.findSources();

        for (JSourceFile source : sources) {
        	indexSource(source);    	
		}
        //todo:index classpaths?
        
        //db.commit();
        
	}
	
	//TODO:add search query
	public void find(){
		ORecordIteratorClass<ODocument> types = db.browseClass("JType");
		for (ODocument doc : types) {
			System.out.println("jtype:" + doc.field("fqdn"));
			System.out.println("     path:" + doc.field("path"));
		}
		//db.query(ONativeQuery<OQueryContextNative>, iArgs)
	}
	
	public void findPatternNamedX(){
		
	}

	public void indexSource(JSourceFile source) {
		Root root = source.getResource().getRoot();
		
		ODocument sourceDoc = new ODocument(); 
		sourceDoc.setClassName("JSource");
		
		sourceDoc.field("root", root.getPathName());
		sourceDoc.field("path", source.getResource().getRelPath());
		
		for(JType type: source.getTopJTypes()){
			indexType(source, sourceDoc, type);
		}
		listener.onSource(source, sourceDoc);
		sourceDoc.save();
	}

	private void indexType(JSourceFile source, ODocument sourceDoc, JType type) {
		ODocument typeDoc = new ODocument();
		typeDoc.addOwner(sourceDoc);
		typeDoc.setClassName("JType");
		
		typeDoc.field("fqdn", type.getFullName());
		typeDoc.field("path", source.getResource().getRelPath());
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
		
		//todo:index implements
		//todo:index extends
		for(JField field: type.findAllFields()){
			indexField(typeDoc, field);
		}
		
		for(JMethod method: type.findAllJMethods()){
			indexMethod(source, type, typeDoc, method);
		}
		listener.onType(type, typeDoc);
		typeDoc.save();
	}

	public void indexField(ODocument typeDoc, JField field) {
		ODocument fieldDoc = new ODocument();
		fieldDoc.addOwner(typeDoc);
		fieldDoc.setClassName("JField");
		for(String name:field.getNames()){
			fieldDoc.field("name", name);
		}
		setModifiersFlags(fieldDoc, field.getJavaModifiers());
		
		listener.onField(field, fieldDoc);
		fieldDoc.save();
	}

	private void indexMethod(JSourceFile source, JType type, ODocument typeDoc,JMethod method) {
		ODocument methodDoc = new ODocument();
		methodDoc.addOwner(typeDoc);
		methodDoc.setClassName("JMethod");
		
		methodDoc.field("fqdn", type.getFullName());
		methodDoc.field("path", source.getResource().getRelPath());
		methodDoc.field("name", method.getName());
		methodDoc.field("numArgs", method.getParameters().size());
		methodDoc.field("isConstructor", method.isConstructor());
		methodDoc.field("isVoid", method.isVoid());
		
		//hasReturns
		//Simple patterns:
		//	isGetter
		//	isSetter
		
		setModifiersFlags(methodDoc, method.getJavaModifiers());
		
		listener.onMethod(method, methodDoc);
		methodDoc.save();
	}
	
	private void setModifiersFlags(ODocument doc, JModifiers mods){
		doc.field("access", mods.asAccess().name());
		doc.field("isFinal", mods.isFinal());
		doc.field("isStatic", mods.isStatic());
		doc.field("isSynchronized", mods.isSynchronized());
	}
	
	@Override
	public void close() throws IOException {
        db.close();
	}
	
	public static interface IndexListener {
		public void onSource(JSourceFile source, ODocument sourceDoc);
		public void onType(JType type, ODocument typeDoc);
		public void onMethod(JMethod method, ODocument methodDoc);
		public void onField(JField field, ODocument fieldDoc);
	}

	public static class NullIndexListener implements IndexListener {
		public void onSource(JSourceFile source, ODocument sourceDoc) {}
		public void onType(JType type, ODocument typeDoc) {}
		public void onMethod(JMethod method, ODocument methodDoc) {}
		public void onField(JField field, ODocument fieldDoc) {}
	}
	
}
