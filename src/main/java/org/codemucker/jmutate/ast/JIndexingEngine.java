package org.codemucker.jmutate.ast;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootVisitor;
import org.codemucker.lang.IBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class JIndexingEngine implements Closeable {

	private final ODatabaseDocumentTx db;
	private final JAstParser parser;

	public static Builder with(){
		return new Builder();
	}
	
	private JIndexingEngine(File dbDirectoy, List<Root> roots, JAstParser parser){
		Preconditions.checkNotNull(dbDirectoy,"expect indexing db directory");
		Preconditions.checkNotNull(roots,"expect roots");
		Preconditions.checkNotNull(parser, "expect parser");
		
		this.parser = parser;
		String dbName = getClass().getSimpleName() + "_db";
        db = Orient.instance()
        	.getDatabaseFactory().createDatabase("graph","memory:" + dbName);

        //TODO:turn off locking while we index
        //TODO:put it in a reusable location so multiple calls find the already indexed code
        
        db.create();
        
        db.declareIntent(new OIntentMassiveInsert());
        
        Object orgValTxUseLog = OGlobalConfiguration.TX_USE_LOG.getValue();
        Object orgValCacheEnabled = OGlobalConfiguration.CACHE_LOCAL_ENABLED.getValue();
        
        OGlobalConfiguration.TX_USE_LOG.setValue(false);
        OGlobalConfiguration.CACHE_LOCAL_ENABLED.setValue(false);
        index(roots);
      
        //reset intent
        db.declareIntent(null);
        OGlobalConfiguration.TX_USE_LOG.setValue(orgValTxUseLog);
        OGlobalConfiguration.CACHE_LOCAL_ENABLED.setValue(orgValCacheEnabled);
	}

	public void index(List<Root> roots) {
		RootVisitor indexer = new JSearchIndexingVisitor(db,parser);
		for(Root root:roots){
			root.accept(indexer);
		}
	}
	
	//TODO:add search query
	public void find(){
		ORecordIteratorClass<ODocument> types = db.browseClass("Type");
		log("browse DB");
		for (ODocument doc : types) {
		    log("fqdn " + doc.field("fqdn"));
			log("     path:" + doc.field("resource"));
		}
		//db.query(ONativeQuery<OQueryContextNative>, iArgs)
		log("end browse DB");
	}
	
	private void log(String msg){
	    //System.out.println(JSearchEngine.class.getName() + " [DEBUG} " + msg);
	}
	
	public void findPatternNamedX(){
		
	}

	@Override
	public void close() {
        db.close();
	}
	
	public static class Builder {
		private File dbDirectory;
		private JAstParser parser;
		private List<Root> roots = Lists.newArrayList();
		
		public JIndexingEngine build(){
			Preconditions.checkNotNull(dbDirectory, "expect a database directory to be set (or use defaults)");
			Preconditions.checkNotNull(parser, "expect a parser to be set (or use defaults)");
			
			return new JIndexingEngine(dbDirectory,roots,parser);
		}
		
		
		public Builder defaults(){
			useDefaultDBDirectory();
			return this;
		}

		public Builder useDefaultDBDirectory(){
			try {
                dbDirectory(File.createTempFile("orientdb_codemucker_search_test",""));
            } catch (IOException e) {
                throw new RuntimeException("Couldn't create tmp directory",e);
            }
			return this;
		}
		
		public Builder useDefaultParser(){
			parser(DefaultJAstParser.with().defaults().build());
			return this;
		}

		public Builder scanRoots(IBuilder<? extends Iterable<Root>> rootsBuilder){
			searchRoots(rootsBuilder.build());
			return this;
		}
		
		public Builder searchRoots(Iterable<Root> roots){
			for(Root r:roots){
				searchRoot(r);
			}
			return this;
		}
		
		public Builder searchRoot(Root root){
			this.roots.add(root);
			return this;
		}
		
		public Builder parser(IBuilder<JAstParser> builder){
			parser(builder.build());
			return this;
		}
		
		public Builder parser(JAstParser parser){
			this.parser = parser;
			return this;
		}
		
		public Builder dbDirectory(String dbDirectory){
			dbDirectory(new File(dbDirectory));
			return this;
		}
		
		public Builder dbDirectory(File dbDirectory){
			this.dbDirectory = dbDirectory;
			return this;
		}
	}
}
