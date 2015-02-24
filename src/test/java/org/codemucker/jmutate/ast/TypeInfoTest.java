package org.codemucker.jmutate.ast;

import org.codemucker.jmatch.AList;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Expect;
import org.junit.Test;

public class TypeInfoTest {


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
		.that(TypeInfo.extractTypeNames(s))
		.is(AList.inOrder().withOnly().items(AString.equalToAll(values)));	
    	
    }
    
}
