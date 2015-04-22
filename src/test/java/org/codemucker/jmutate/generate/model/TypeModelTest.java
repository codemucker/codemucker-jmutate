package org.codemucker.jmutate.generate.model;

import org.codemucker.jmatch.AList;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.generate.model.TypeModel;
import org.junit.Test;

public class TypeModelTest {


    @Test
    public void extractTypeBoundParamNames(){
    	extractParamNamesIs("<Foo,Bar>","Foo","Bar");
    	extractParamNamesIs("< Foo,Bar>","Foo","Bar");
    	extractParamNamesIs("< Foo ,Bar>","Foo","Bar");
    	extractParamNamesIs("<Foo, Bar>","Foo","Bar");
    	extractParamNamesIs("<Foo,Bar >","Foo","Bar");
    	extractParamNamesIs("<  Foo  ,  Bar  >","Foo","Bar");
    	
    	extractParamNamesIs("<Foo,Bar extends Goofy>>","Foo","Bar");
		extractParamNamesIs("<Foo,Bar extends Goofy<?>>","Foo","Bar");
		extractParamNamesIs("<Foo,Bar extends Goofy<Foo,Alice>>","Foo","Bar");
		extractParamNamesIs("<Foo,Bar extends Goofy<Foo,Alice>,Bob>","Foo","Bar","Bob");	
		extractParamNamesIs("<Foo,Bar extends  Goofy < Foo , Alice >, Bob >","Foo","Bar","Bob");	
	    
    }
    
    private static void extractParamNamesIs(String s,String...values){
    	Expect
		.that(TypeModel.extractTypeNames(s))
		.is(AList.inOrder().withOnly().items(AString.equalToAll(values)));	
    	
    }
    
    @Test
    public void mapValuesSet(){
    	for(String type: new String[]{"java.util.Map<K,V>", "java.util.HashMap<K,V>","java.util.TreeMap<K,V>"}){
	    	TypeModel t = new TypeModel(type);
	    	Expect.that(t.isMap()).isTrue();
	    	Expect.that(t.isKeyed()).isTrue();
	    	Expect.that(t.isCollection()).isFalse();
	    	Expect.that(t.isArray()).isFalse();
	    	Expect.that(t.getIndexedKeyTypeNameOrNull()).isEqualTo("K");
	    	Expect.that(t.getIndexedValueTypeNameOrNull()).isEqualTo("V");
	    }
    }
    
    
    @Test
    public void collectionValueSet(){
    	for(String type: new String[]{"java.util.List<V>","java.util.ArrayList<V>","java.util.HashSet<V>"}){
	    	TypeModel t = new TypeModel(type);
	    	Expect.that(t.isMap()).isFalse();
	    	Expect.that(t.isCollection()).isTrue();
	    	Expect.that(t.isArray()).isFalse();
	    	Expect.that(t.isKeyed()).isFalse();
	    	Expect.that(t.getIndexedKeyTypeNameOrNull()).isNull();
	    	Expect.with().failureMessage(type).that(t.getIndexedValueTypeNameOrNull()).isEqualTo("V");
	    }
    }
}
