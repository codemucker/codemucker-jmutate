package com.bertvanbrakel.codemucker.transform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jdt.core.dom.ASTNode;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.Flattener;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.google.inject.Inject;

/**
 * Builds a method to generate the given node using a source template
 */
public class NodeToSourceBuilder {

	@Inject
	private MutationContext ctxt;

	@Inject
	private Flattener flattener;
	
	private ASTNode node;

	public JMethod build(){
	
		String src = flattener.flatten(node);
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.p("public void generateNode(")
		 .p(MutationContext.class.getName())
		 .p(" ctxt")
		 .pl("){")
		 .p(SourceTemplate.class.getName()).p(" t = ctxt.newSourceTemplate();");
	
		BufferedReader reader = new BufferedReader(new StringReader(src));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				t.p(" t.pl(\"").p(escapeSrc(line)).pl("\");");
			}
        } catch (IOException e) {
	        //never thrown
        	throw new CodemuckerException("error reading source:" + src, e);
        }
		t.pl("}");
		
		return t.asJMethod();
	}
	
	private static String escapeSrc(String line){
		return line.replaceAll("\"", "\\\"");
	}
	
	public NodeToSourceBuilder setFlattener(Flattener flattener) {
		this.flattener = flattener;
		return this;
	}
	
	public NodeToSourceBuilder setCtxt(MutationContext ctxt) {
		this.ctxt = ctxt;
		return this;
	}

	public NodeToSourceBuilder setNode(ASTNode node) {
		this.node = node;
		return this;
	}
}
