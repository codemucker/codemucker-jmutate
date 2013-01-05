package com.bertvanbrakel.codemucker.ast;

import java.io.Closeable;
import java.util.List;

import com.bertvanbrakel.codemucker.ast.finder.JSearchScope;
import com.bertvanbrakel.codemucker.ast.finder.JSearchScopeVisitor;
import com.bertvanbrakel.test.finder.Root;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class JSearchEngine implements Closeable {

	private final OGraphDatabase db;
	
	public JSearchEngine(List<Root> roots){
        db = Orient.instance()
        	.getDatabaseFactory()
        	.createGraphDatabase("local:c:/tmp/orientdb_codemucker_search_test" + System.currentTimeMillis());
        
        //TODO:turn off locking while we index
        //TODO:put it in a reusable location so multiple calls find the already indexed code
        
        db.create();
        
        db.declareIntent(new OIntentMassiveInsert());
        OGlobalConfiguration.TX_USE_LOG.setValue(false);
        OGlobalConfiguration.CACHE_LEVEL1_ENABLED.setValue(false);
        OGlobalConfiguration.CACHE_LEVEL2_ENABLED.setValue(false);
        index(roots);
      
        //reset intent
        db.declareIntent(null);
        OGlobalConfiguration.TX_USE_LOG.setValue(true);
        OGlobalConfiguration.CACHE_LEVEL1_ENABLED.setValue(true);
        OGlobalConfiguration.CACHE_LEVEL2_ENABLED.setValue(true);
	}

	public void index(List<Root> roots) {
		//TODO:add search indexers here. Allow custom indexers fro source and non java files
		JSearchScope searchScope = JSearchScope.builder().setSearchRoots(roots).build();
        JSearchScopeVisitor indexer = new JSearchIndexingVisitor(db,searchScope.getParser());
        
        searchScope.accept(indexer);
	}
	
	//TODO:add search query
	public void find(){
		ORecordIteratorClass<ODocument> types = db.browseClass("Type");
		for (ODocument doc : types) {
			System.out.println("fqdn " + doc.field("fqdn"));
			System.out.println("     path:" + doc.field("resource"));
		}
		//db.query(ONativeQuery<OQueryContextNative>, iArgs)
	}
	
	public void findPatternNamedX(){
		
	}

	@Override
	public void close() {
        db.close();
	}
}
