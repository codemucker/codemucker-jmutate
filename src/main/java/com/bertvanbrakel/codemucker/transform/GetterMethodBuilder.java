package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.util.TypeUtil;
import com.bertvanbrakel.test.util.ClassNameUtil;

public final class GetterMethodBuilder extends AbstractPatternBuilder<GetterMethodBuilder> {

	private String fieldName;
	private String fieldType;
	private boolean cloneOnReturn;
	private JAccess access = JAccess.PUBLIC;
	
	public static GetterMethodBuilder newBuilder(){
		return new GetterMethodBuilder();
	}
	
	public GetterMethodBuilder(){
		setPattern("bean.getter");
	}
	
	public JMethod build(){
		checkFieldsSet();
		checkState(!StringUtils.isBlank(fieldName), "missing name");
		checkState(!StringUtils.isBlank(fieldType), "missing type");

		return new JMethod(toMethod());
	}
	
	private MethodDeclaration toMethod(){
		SourceTemplate template = getContext().newSourceTemplate();
		
		String upperName = ClassNameUtil.upperFirstChar(fieldName);
		template
			.setVar("methodName", "get" + upperName)
			.setVar("fieldType", fieldType)
			.setVar("fieldName", fieldName);

		if(isMarkedGenerated()){
			template
				.p('@')
				.p(Pattern.class.getName())
				.p("(name=\"")
				.p(getPattern())
				.pl("\")");
		}
		template.p(access.toCode()).p(" ${fieldType} ${methodName}(){");
		boolean clone = cloneOnReturn && !TypeUtil.isPrimitive(fieldType); 
		if(clone){
    		template.pl("return this.${fieldName}==null?null:this.${fieldName}.clone();");
		} else {
			template.pl("return this.${fieldName};");
		}
		template.p("}");
		return template.asMethodNode();
	}
	
	public GetterMethodBuilder setFromField(JField f) {
		setFieldName(f.getName());
		setFieldType(f.getTypeSignature());
		return this;
	}

	public GetterMethodBuilder setAccess(JAccess access) {
    	this.access  = access;
    	return this;
    }

	public GetterMethodBuilder setFieldName(String name) {
		this.fieldName = name;
		return this;
	}

	public GetterMethodBuilder setFieldType(String type) {
		this.fieldType = type;
		return this;
	}
}