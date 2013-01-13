package com.bertvanbrakel.codemucker.transform;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;

import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.ast.AstNodeFlattener;
import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JAstFlattener;
import com.bertvanbrakel.codemucker.ast.JField;

public class FieldBuilder extends AbstractBuilder<FieldBuilder> {

	public static enum RETURN {
		VOID, TARGET, ARG;
	}

	private String name;
	private String type;
	private JAccess access = JAccess.PRIVATE;
	private Expression initializer;

	public static FieldBuilder builder(){
		return new FieldBuilder();
	}
	
	public FieldBuilder(){
		setPattern("bean.property");
	}
	
	public JField build(){
		checkFieldsSet();
		checkNotNull("name", name);
		checkNotNull("type", type);

		return JField.from(toFieldNode());
	}
	
	private FieldDeclaration toFieldNode(){
		SourceTemplate t = getContext().newSourceTemplate()
			.setVar("fieldName", name)
			.setVar("fieldType", type);

		if(isMarkedGenerated()){
			t.p('@')
			.p(Pattern.class.getName())
			.p("(name=\"")
			.p(getPattern())
			.pl("\")");
		}
		t.print(access.toCode());
		t.print(" ${fieldType} ${fieldName}");
		if( initializer != null){
			String valAsString = getContext().obtain(AstNodeFlattener.class).flatten(initializer);
			t.setVar("initializer", valAsString);
			t.pl(" = ${initializer};");
		} else {
			t.pl(";");
		}
	
		return t.asResolvedFieldNode();
	}

	public FieldBuilder setFieldAccess(JAccess access) {
    	this.access  = access;
    	return this;
    }

	public FieldBuilder setFieldName(String name) {
		this.name = name;
		return this;
	}

	public FieldBuilder setFieldType(String type) {
		this.type = type;
		return this;
	}
	
	public FieldBuilder setFieldType(Type type) {
		this.type = JAstFlattener.asString(type);
		return this;
	}
	
	public FieldBuilder setFieldInitializer(String value) {
		this.initializer = getContext()
			.newSourceTemplate()
			.p(value)
			.asExpressionNode();
		return this;
	}
	
	public FieldBuilder setFieldInitializer(Expression expression) {
		this.initializer = expression;
		return this;
	}
}
