package org.codemucker.jmutate.transform;

import static com.bertvanbrakel.lang.Check.checkNotBlank;
import static com.bertvanbrakel.lang.Check.checkNotNull;
import static com.bertvanbrakel.lang.Check.checkTrue;

import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.test.util.ClassNameUtil;

public final class SetterMethodBuilder extends AbstractBuilder<SetterMethodBuilder> {

	public static enum RETURN {
		VOID, TARGET, ARG;
	}

	private RETURN returnType = RETURN.VOID;
	private JAccess access = JAccess.PUBLIC;

	private String name;
	private String type;
	private JType target;

	public static SetterMethodBuilder builder() {
		return new SetterMethodBuilder();
	}

	public SetterMethodBuilder(){
		setPattern("bean.setter");
	}
	
	public JMethod build() {
		checkFieldsSet();
		
		checkNotNull("target", target);
		checkNotBlank("name", name);
		checkNotBlank("type", type);

		return JMethod.from(toMethod());
	}

	private MethodDeclaration toMethod() {
		SourceTemplate template = getContext().newSourceTemplate();

		String upperName = ClassNameUtil.upperFirstChar(name);
		template
			.setVar("methodName", "set" + upperName)
			.setVar("argType", type)
			.setVar("argName", name)
		    .setVar("fieldName", name);

		if (isMarkedGenerated()) {
			template.p('@')
    			.p(Pattern.class.getName())
    			.p("(name=\"")
    			.p(getPattern())
    			.pl("\")");
		}
		template.print(access.toCode());
		template.println(" ${returnType} ${methodName}(${argType} ${argName}) {");
		template.println("this.${fieldName} = ${argName};");
		switch (returnType) {
		case ARG:
			template.setVar("returnType", type);
			template.println("return ${argName};");
			break;
		case TARGET:
			template.setVar("returnType", target.getSimpleName());
			template.println("return this;");
			break;
		case VOID:
			template.setVar("returnType", "void");
			break;
		default:
			checkTrue("returnType",returnType, false, "unknown type");
		}
		template.println("}");

		return template.asResolvedMethodNode();
	}

	public SetterMethodBuilder setMethodAccess(JAccess access) {
		this.access = access;
		return this;
	}

	public SetterMethodBuilder setFromField(JField f) {
		setFieldName(f.getName());
		setFieldType(f.getTypeSignature());
		return this;
	}

	public SetterMethodBuilder setFieldName(String name) {
		this.name = name;
		return this;
	}

	public SetterMethodBuilder setFieldType(String type) {
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
