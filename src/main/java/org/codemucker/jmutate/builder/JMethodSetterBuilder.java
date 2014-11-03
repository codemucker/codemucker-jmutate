package org.codemucker.jmutate.builder;

import static org.codemucker.lang.Check.checkNotBlank;
import static org.codemucker.lang.Check.checkNotNull;
import static org.codemucker.lang.Check.checkTrue;

import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.Pattern;
import org.codemucker.lang.ClassNameUtil;
import org.codemucker.lang.annotation.Optional;
import org.codemucker.lang.annotation.Required;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

public final class JMethodSetterBuilder extends AbstractBuilder<JMethodSetterBuilder,JMethod> {

	public static enum RETURNS {
		VOID, TARGET, ARG;
	}

	private RETURNS returnType = RETURNS.VOID;
	private JAccess access = JAccess.PUBLIC;

	@Optional
	private String methodName;//defaults if not set    
	
	@Required
	private String fieldName;
	@Required
    private String fieldType;
	@Required
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
		checkNotBlank("name", fieldName);
		checkNotBlank("type", fieldType);

		return JMethod.from(toMethod());
	}

	private MethodDeclaration toMethod() {
		SourceTemplate template = getContext().newSourceTemplate();

		String setterName = methodName;
		if(setterName == null){
            setterName = "set" + ClassNameUtil.upperFirstChar(fieldName);
        }
	      
		template
			.setVar("methodName", setterName)
			.setVar("argType", fieldType)
			.setVar("argName", fieldName)
		    .setVar("fieldName", fieldName);

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

	@Optional
	public JMethodSetterBuilder methodAccess(JAccess access) {
		this.access = access;
		return this;
	}

	/**
	 * Sets both {@link #fieldName(String)}} and {@link #fieldType(String)}} at once
	 */
	public JMethodSetterBuilder field(JField f) {
		fieldName(f.getName());
		fieldType(f.getTypeSignature());
		return this;
	}

	@Required
	public JMethodSetterBuilder fieldName(String name) {
		this.fieldName = name;
		return this;
	}

	@Required
	public JMethodSetterBuilder fieldType(Type type) {
		fieldType(NameUtil.resolveQualifiedName(type));
		return this;
	}
	
	@Required
	public JMethodSetterBuilder fieldType(String type) {
		this.fieldType = NameUtil.toShortNameIfDefaultImport(type);
		return this;
	}

	@Optional
    public JMethodSetterBuilder methodName(String name) {
        this.methodName = name;
        return this;
    }
	
	@Optional
	public JMethodSetterBuilder returns(RETURNS returnType) {
		this.returnType = returnType;
		return this;
	}

	@Required
	public JMethodSetterBuilder target(JType target) {
		this.target = target;
		return this;
	}
}
