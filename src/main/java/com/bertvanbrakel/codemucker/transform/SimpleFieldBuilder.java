package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import org.eclipse.jdt.core.dom.FieldDeclaration;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.util.TypeUtil;

public class SimpleFieldBuilder {

	public static enum RETURN {
		VOID, TARGET, ARG;
	}

	private boolean markedGenerated;
	private String pattern;
	private String name;
	private String type;
	private JType target;
	private MutationContext context;
	private JAccess access = JAccess.PUBLIC;
	private Object defaultValue;
	private int modifiers;

	public JField build(){
		checkState(context != null, "missing context");
		checkState(target != null, "missing target");
		checkState(name != null, "missing name");
		checkState(type != null, "missing type");

		return new JField(toFieldNode());
	}
	
	private FieldDeclaration toFieldNode(){
		SourceTemplate t = context.newSourceTemplate();
		t
			.setVar("fieldName", name)
			.setVar("fieldType", type);

		if( markedGenerated){
			t.print("@Pattern(name=\"");
			t.print(pattern);
			t.print('"');
			t.println(')');
		}
		t.print(access.toCode());
		t.print(" ${fieldType} ${fieldName}");
		if( defaultValue != null){
			t.setVar("defaultValue", defaultValue);
			t.println(" = ${quote}${defaultValue}${quote};");
			t.setVar("quote", getQuoteCharForValue());
		} else {
			t.println(";");
		}
	
		FieldDeclaration field = t.asFieldNode();
		return field;
	}
	
	private String getQuoteCharForValue(){
		if( TypeUtil.typeValueRequiresDoubleQuotes(type)){
			return "\"";
		} else if(  TypeUtil.typeValueRequiresDoubleQuotes(type)){
			return "'";
		} else {
			return "";
		}
	}

	public SimpleFieldBuilder setAccess(JAccess access) {
    	this.access  = access;
    	return this;
    }

	public SimpleFieldBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public SimpleFieldBuilder setType(String type) {
		this.type = type;
		return this;
	}
}
