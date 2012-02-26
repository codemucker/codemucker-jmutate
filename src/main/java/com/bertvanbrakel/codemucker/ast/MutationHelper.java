package com.bertvanbrakel.codemucker.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.bean.BeanGenerationException;
import com.bertvanbrakel.lang.interpolator.Interpolator;

public abstract class AbstractMutation<T> extends AbstractMutationBuilder<T> {

	private final JContext context;
	
	AbstractMutation(JContext context){
		this.context = context;
	}

	protected void addToBodyUsingStrategy(JType javaType, ASTNode child, InsertionStrategy strategy) {
		ASTNode copy = ASTNode.copySubtree(javaType.getAst(), child);
		List<ASTNode> body = javaType.getBodyDeclarations();
		int index = strategy.findIndex(body);
		if( index < 0){
			throw new CodemuckerException("Insertion strategy %s couldn't find an index to insert %s into", strategy, child);
		}
		body.add(index, copy);
	}
	
	protected FieldDeclaration parseField(String fieldSnippet){
		String src = wrapSnippetInClassDeclaration(fieldSnippet);
		TypeDeclaration type = parseClass(src);
		FieldDeclaration fieldNode = type.getFields()[0];
		return fieldNode;
	}
	
	protected MethodDeclaration parseConstructor(String ctorSnippet){
		MethodDeclaration method = parseMethod(ctorSnippet);
		if (method.getReturnType2() != null) {
			throw new BeanGenerationException("Constructors should not have any return type. Constructor was %s",
			        method);
		}
		method.setConstructor(true);
		return method;
	}
	
	protected MethodDeclaration parseMethod(String methodSnippet){
		String src = wrapSnippetInClassDeclaration(methodSnippet);

		TypeDeclaration type = parseClass(src);
		MethodDeclaration method = type.getMethods()[0];
		
		return method;
	}
	
	private String wrapSnippetInClassDeclaration(String snippetSrc) {
		String simpleClassName = "AutoKlass__";
		String wrappedSrc = "class " + simpleClassName + "{" + snippetSrc + "}";
		return wrappedSrc;
	}
	
	protected TypeDeclaration parseClass(String snippetSrc) {
		CompilationUnit cu = parseCompilationUnit(snippetSrc);
		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);

		return type;
	}

	protected CompilationUnit parseCompilationUnit(String snippetSrc) {
		// get template variables and interpolate
		//TODO:add defaults vars
		Map<String, Object> vars = new HashMap<String, Object>();
		CharSequence interpolatedSrc = Interpolator.interpolate(snippetSrc, vars);
		// parse it
		//TODO:jealous class, knows too much about the javaType/ASTCreator. Use a generation context?
		CompilationUnit cu = context.getAstCreator().parseCompilationUnit(interpolatedSrc);
		return cu;
	}

	
}
