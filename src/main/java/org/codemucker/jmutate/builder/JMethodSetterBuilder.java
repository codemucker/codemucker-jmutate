package org.codemucker.jmutate.builder;

import static org.codemucker.lang.Check.checkNotBlank;
import static org.codemucker.lang.Check.checkNotNull;
import static org.codemucker.lang.Check.checkTrue;

import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.codemucker.jmutate.util.TypeUtil;
import org.codemucker.jpattern.Pattern;
import org.codemucker.jtest.ClassNameUtil;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

public final class JMethodSetterBuilder extends AbstractBuilder<JMethodSetterBuilder,JMethod> {

	public static enum RETURNS {
		VOID, TARGET, ARG;
	}

	private RETURNS returnType = RETURNS.VOID;
	private JAccess access = JAccess.PUBLIC;

	private String name;
	private String fieldType;
	private JType target;

	public static JMethodSetterBuilder with() {
		return new JMethodSetterBuilder();
	}

	public JMethodSetterBuilder(){
		pattern("bean.setter");
	}
	
	@Override
	public JMethod build() {
		checkFieldsSet();
		
		checkNotNull("target", target);
		checkNotBlank("name", name);
		checkNotBlank("type", fieldType);

		return JMethod.from(toMethod());
	}

	private MethodDeclaration toMethod() {
		SourceTemplate template = getContext().newSourceTemplate();

		String upperName = ClassNameUtil.upperFirstChar(name);
		template
			.setVar("methodName", "set" + upperName)
			.setVar("argType", fieldType)
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
			template.setVar("returnType", fieldType);
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

	public JMethodSetterBuilder methodAccess(JAccess access) {
		this.access = access;
		return this;
	}

	public JMethodSetterBuilder field(JField f) {
		fieldName(f.getName());
		fieldType(f.getTypeSignature());
		return this;
	}

	public JMethodSetterBuilder fieldName(String name) {
		this.name = name;
		return this;
	}

	public JMethodSetterBuilder fieldType(Type type) {
		fieldType(JavaNameUtil.resolveQualifiedName(type));
		return this;
	}
	
	public JMethodSetterBuilder fieldType(String type) {
		this.fieldType = TypeUtil.toShortNameIfDefaultImport(type);
		return this;
	}

	public JMethodSetterBuilder returns(RETURNS returnType) {
		this.returnType = returnType;
		return this;
	}

	public JMethodSetterBuilder target(JType target) {
		this.target = target;
		return this;
	}
}
