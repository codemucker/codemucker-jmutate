package org.codemucker.jmutate.builder;

import static org.codemucker.lang.Check.checkNotBlank;

import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.util.TypeUtil;
import org.codemucker.jpattern.Pattern;
import org.codemucker.jtest.ClassNameUtil;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public final class JMethodGetterBuilder extends AbstractBuilder<JMethodGetterBuilder,JMethod> {

	private String fieldName;
	private String fieldType;
	private boolean cloneOnReturn;
	private JAccess access = JAccess.PUBLIC;
	
	public static JMethodGetterBuilder with(){
		return new JMethodGetterBuilder();
	}
	
	public JMethodGetterBuilder(){
		pattern("bean.getter");
	}
	
	@Override
	public JMethod build(){
		checkFieldsSet();
		checkNotBlank("fieldName", fieldName);
		checkNotBlank("fieldType", fieldType);

		return JMethod.from(toMethod());
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
				.p(Pattern.class)
				.p("(name=\"")
				.p(getPattern())
				.pl("\")");
		}
		template.p(access.toCode()).p(" ${fieldType} ${methodName}(){").pl();
		boolean clone = cloneOnReturn && !TypeUtil.isPrimitive(fieldType); 
		if(clone){
    		template.pl("return this.${fieldName}==null?null:this.${fieldName}.clone();");
		} else {
			template.pl("return this.${fieldName};");
		}
		template.p("}");
		return template.asMethodNodeSnippet();
	}

	public JMethodGetterBuilder methodAccess(JAccess access) {
    	this.access  = access;
    	return this;
    }

	public JMethodGetterBuilder field(JField f) {
		fieldName(f.getName());
		fieldType(f.getTypeSignature());
		return this;
	}
	
	public JMethodGetterBuilder fieldName(String name) {
		this.fieldName = name;
		return this;
	}

	public JMethodGetterBuilder fieldType(String type) {
		this.fieldType = TypeUtil.toShortNameIfDefaultImport(type);
		return this;
	}
}
