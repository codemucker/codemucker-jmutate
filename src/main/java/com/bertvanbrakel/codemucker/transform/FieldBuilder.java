package com.bertvanbrakel.codemucker.transform;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;

import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.ast.Flattener;
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
	private int modifiers;

	public static FieldBuilder newBuilder(){
		return new FieldBuilder();
	}
	
	public FieldBuilder(){
		setPattern("bean.property");
	}
	
	public JField build(){
		checkFieldsSet();
		checkNotNull("name", name);
		checkNotNull("type", type);

		return new JField(toFieldNode());
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
			String valAsString = getContext().obtain(Flattener.class).flatten(initializer);
			t.setVar("initializer", valAsString);
			t.pl(" = ${initializer};");
		} else {
			t.pl(";");
		}
	
		return t.asFieldNode();
	}

	public FieldBuilder setAccess(JAccess access) {
    	this.access  = access;
    	return this;
    }

	public FieldBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public FieldBuilder setType(String type) {
		this.type = type;
		return this;
	}
	
	public FieldBuilder setType(Type type) {
		this.type = JAstFlattener.asString(type);
		return this;
	}
	
	public FieldBuilder setInitializer(String value) {
		this.initializer = getContext()
			.newSourceTemplate()
			.p(value)
			.asExpressionNode();
		return this;
	}
	
	public FieldBuilder setInitializer(Expression expression) {
		this.initializer = expression;
		return this;
	}
}
