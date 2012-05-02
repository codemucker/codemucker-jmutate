package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.FieldDeclaration;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.util.TypeUtil;

public class SimpleFieldBuilder extends AbstractPatternBuilder<SimpleFieldBuilder> {

	public static enum RETURN {
		VOID, TARGET, ARG;
	}

	private String name;
	private String type;
	private JType target;
	private JAccess access = JAccess.PUBLIC;
	private Object defaultValue;
	private int modifiers;

	public static SimpleFieldBuilder newBuilder(){
		return new SimpleFieldBuilder();
	}
	
	public SimpleFieldBuilder(){
		setPattern("bea.property");
	}
	
	public JField build(){
		checkFieldsSet();
		checkState(target != null, "missing target");
		checkState(!StringUtils.isBlank(name), "missing name");
		checkState(!StringUtils.isBlank(type), "missing type");

		return new JField(toFieldNode());
	}
	
	private FieldDeclaration toFieldNode(){
		SourceTemplate t = getContext()
			.newSourceTemplate()
			.setVar("fieldName", name)
			.setVar("fieldType", type);

		if(isMarkedGenerated()){
			t.p("@Pattern(name=\"")
			.p(getPattern())
			.p('"')
			.p(')');
		}
		t.print(access.toCode());
		t.print(" ${fieldType} ${fieldName}");
		if( defaultValue != null){
			t.setVar("defaultValue", defaultValue);
			t.pl(" = ${quote}${defaultValue}${quote};");
			t.setVar("quote", getQuoteCharForValue());
		} else {
			t.pl(";");
		}
	
		FieldDeclaration field = t.asFieldNode();
		return field;
	}
	
	private String getQuoteCharForValue(){
		if( TypeUtil.typeValueRequiresDoubleQuotes(type)){
			return "\"";
		}
		if( TypeUtil.typeValueRequiresDoubleQuotes(type)){
			return "'";
		}
		return "";
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
