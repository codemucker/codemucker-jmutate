package com.bertvanbrakel.codemucker.transform;

import static com.bertvanbrakel.lang.Check.checkNotNull;

import org.eclipse.jdt.core.dom.FieldDeclaration;

import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.util.TypeUtil;

public class SimpleFieldBuilder extends AbstractPatternBuilder<SimpleFieldBuilder> {

	public static enum RETURN {
		VOID, TARGET, ARG;
	}

	private String name;
	private String type;
	private JAccess access = JAccess.PRIVATE;
	private Object defaultValue;
	private int modifiers;

	public static SimpleFieldBuilder newBuilder(){
		return new SimpleFieldBuilder();
	}
	
	public SimpleFieldBuilder(){
		setPattern("bean.property");
	}
	
	public JField build(){
		checkFieldsSet();
		checkNotNull("name", name);
		checkNotNull("type", type);

		return new JField(toFieldNode());
	}
	
	private FieldDeclaration toFieldNode(){
		SourceTemplate t = getContext()
			.newSourceTemplate()
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
		if( defaultValue != null){
			t.setVar("defaultValue", defaultValue);
			t.pl(" = ${quote}${defaultValue}${quote};");
			t.setVar("quote", getQuoteCharForValue());
		} else {
			t.pl(";");
		}
	
		return t.asFieldNode();
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
