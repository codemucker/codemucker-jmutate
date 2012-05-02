package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.test.util.ClassNameUtil;

public final class SetterMethodBuilder extends AbstractPatternBuilder<SetterMethodBuilder> {

	public static enum RETURN {
		VOID, TARGET, ARG;
	}

	private RETURN returnType = RETURN.VOID;
	private JAccess access = JAccess.PUBLIC;

	private String name;
	private String type;
	private JType target;

	public static SetterMethodBuilder newBuilder() {
		return new SetterMethodBuilder();
	}

	public SetterMethodBuilder(){
		setPattern("bean.setter");
	}
	
	public JMethod build() {
		checkFieldsSet();
		
		checkState(target != null, "missing target");
		checkState(!StringUtils.isBlank(name), "missing name");
		checkState(!StringUtils.isBlank(type), "missing type");

		return new JMethod(toMethod());
	}

	private MethodDeclaration toMethod() {
		SourceTemplate template = getContext().newSourceTemplate();

		String upperName = ClassNameUtil.upperFirstChar(name);
		template.setVar("methodName", "set" + upperName).setVar("argType", type).setVar("argName", name)
		        .setVar("fieldName", name);

		if (isMarkedGenerated()) {
			template.print('@');
			template.print(Pattern.class.getName());
			template.print("(name=\"");
			template.print(getPattern());
			template.println("\")");
		}
		template.print(access.toCode());
		template.println(" ${returnType} ${methodName}(${argType} ${argName}) {");
		template.println("this.${fieldName} = ${argName};");
		switch (returnType) {
		case ARG:
			template.setVar("returnType", type);
			template.println("return this;");
			break;
		case TARGET:
			template.setVar("returnType", target.getSimpleName());
			template.println("return ${argName};");
			break;
		case VOID:
			template.setVar("returnType", "void");
			break;
		default:
			checkArgument(false, "don't know how to handle return type:" + returnType);
		}
		template.println("}");

		return template.asMethodNode();
	}

	public SetterMethodBuilder setAccess(JAccess access) {
		this.access = access;
		return this;
	}

	public SetterMethodBuilder setFromField(JField f) {
		setName(f.getName());
		setType(f.getTypeSignature());
		return this;
	}

	public SetterMethodBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public SetterMethodBuilder setType(String type) {
		this.type = type;
		return this;
	}

	public SetterMethodBuilder setReturnType(RETURN returnType) {
		this.returnType = returnType;
		return this;
	}

	public SetterMethodBuilder setTarget(JType target) {
		this.target = target;
		return this;
	}


}
