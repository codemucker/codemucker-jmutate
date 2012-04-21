package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.util.TypeUtil;
import com.bertvanbrakel.test.util.ClassNameUtil;

public class GetterMethodBuilder {

	private boolean markedGenerated;
	private String pattern = "bean.getter";
	private String fieldName;
	private String fieldType;
	private JType target;
	private boolean cloneOnReturn;
	private MutationContext context;
	private JAccess access = JAccess.PUBLIC;
	
	public JMethod build(){
		checkState(context != null, "missing context");
		checkState(target != null, "missing target");
		checkState(fieldName != null, "missing name");
		checkState(fieldType != null, "missing type");

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
			template.print("@Pattern(name=\"");
			template.print(pattern);
			template.println("\")");
		}
		template.print(access.toCode());
		template.println(" ${fieldType} ${methodName}(${fieldType}(){");
		if( !TypeUtil.isPrimitive(fieldType)){
    		if(cloneOnReturn){
    			template.println("return this.${fieldName}==null?null:this.${fieldName}.clone();");
    		} else {
    			template.println("return this.${fieldName};");	
    		}
		}
		template.println("}");
		
		return template.asMethodNode();
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

	public void setMarkedGenerated(boolean markedGenerated) {
    	this.markedGenerated = markedGenerated;
    }

	public GetterMethodBuilder setTarget(JType target) {
    	this.target = target;
    	return this;
	}

	public GetterMethodBuilder setContext(MutationContext context) {
    	this.context = context;
    	return this;
    }
}
