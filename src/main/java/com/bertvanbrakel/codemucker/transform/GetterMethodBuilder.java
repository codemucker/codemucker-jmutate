package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.help.internal.util.StringUtil;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.util.TypeUtil;
import com.bertvanbrakel.test.util.ClassNameUtil;

public class GetterMethodBuilder {

	private boolean markedGenerated;
	private String pattern = "bean.getter";
	private String fieldName;
	private String fieldType;
	private boolean cloneOnReturn;
	private MutationContext context;
	private JAccess access = JAccess.PUBLIC;
	
	
	public static GetterMethodBuilder newBuilder(){
		return new GetterMethodBuilder();
	}
	
	public JMethod build(){
		checkState(context != null, "missing context");
		checkState(!StringUtils.isBlank(fieldName), "missing name");
		checkState(!StringUtils.isBlank(fieldType), "missing type");

		return new JMethod(toMethod());
	}
	
	private MethodDeclaration toMethod(){
		SourceTemplate template = context.newSourceTemplate();
		
		String upperName = ClassNameUtil.upperFirstChar(fieldName);
		template
			.setVar("methodName", "get" + upperName)
			.setVar("fieldType", fieldType)
			.setVar("fieldName", fieldName);

		if( markedGenerated){
			template
				.p('@')
				.p(Pattern.class.getName())
				.p("(name=\"")
				.p(pattern)
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

	public GetterMethodBuilder setMarkedGenerated(boolean markedGenerated) {
    	this.markedGenerated = markedGenerated;
    	return this;
	}

	public GetterMethodBuilder setContext(MutationContext context) {
    	this.context = context;
    	return this;
    }
}
