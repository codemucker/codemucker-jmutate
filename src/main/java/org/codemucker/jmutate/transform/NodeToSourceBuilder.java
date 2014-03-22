package org.codemucker.jmutate.transform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.codemucker.jmutate.ast.AstNodeFlattener;
import org.codemucker.jmutate.ast.CodemuckerException;
import org.codemucker.jmutate.ast.JMethod;
import org.eclipse.jdt.core.dom.ASTNode;

import com.google.inject.Inject;

/**
 * Builds a method to generate the given node source using a source template. This means, pass an existing source node (i.e
 * some existing code) and convert this into a method which when runs regenerates the original node source. Useful when
 * wanting to take existing code and turn it into a template.
 */
public class NodeToSourceBuilder {

	@Inject
	private CodeMuckContext ctxt;

	@Inject
	private AstNodeFlattener flattener;
	
	//the node to copy
	private ASTNode node;

	public JMethod build(){
		String src = flattener.flatten(node);
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.p("public void generateNode(").p(CodeMuckContext.class.getName()).p(" ctxt").pl("){")
		 .p(SourceTemplate.class.getName()).p(" t = ctxt.newSourceTemplate();");
	
		BufferedReader reader = new BufferedReader(new StringReader(src));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				t.p(" t.pl(\"").p(escapeSource(line)).pl("\");");
			}
        } catch (IOException e) {
	        //never thrown
        	throw new CodemuckerException("error reading source:" + src, e);
        }
		t.pl("}");
		
		return t.asResolvedJMethod();
	}
	
	private static String escapeSource(String line){
		return line.replaceAll("\"", "\\\"");
	}
	
	public NodeToSourceBuilder setFlattener(AstNodeFlattener flattener) {
		this.flattener = flattener;
		return this;
	}
	
	public NodeToSourceBuilder setCtxt(CodeMuckContext ctxt) {
		this.ctxt = ctxt;
		return this;
	}

	public NodeToSourceBuilder setNode(ASTNode node) {
		this.node = node;
		return this;
	}
}
