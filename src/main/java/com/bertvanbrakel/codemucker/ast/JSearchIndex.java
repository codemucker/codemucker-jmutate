package com.bertvanbrakel.codemucker.ast;

import java.util.List;

import com.bertvanbrakel.test.finder.Root;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class JSearchIndex {

	public JSearchIndex(List<Root> roots){
        final OGraphDatabase db = Orient.instance()
        			.getDatabaseFactory()
        			.createGraphDatabase("local:/tmp/orientdb_codemucker_search_test" + System.currentTimeMillis());
        db.create();
       // db.begin();

        Person p = Person.newBuilder()
        	.setFirstName("my first name")
        	.setLastName("my last name")
        	.build();
        
        p.toDocument().save();
        
        
        ORecordIteratorClass<ODocument> people = db.browseClass("Person");
        for( ODocument doc:people){
        	Person person = Person.newFromDocument(doc);
        	
        }
        //db.commit();
        db.close();
	}
}
