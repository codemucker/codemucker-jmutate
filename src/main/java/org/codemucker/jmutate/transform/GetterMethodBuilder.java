package org.codemucker.jmutate.transform;

import static com.bertvanbrakel.lang.Check.checkNotBlank;

import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.util.TypeUtil;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.test.util.ClassNameUtil;

public final class GetterMethodBuilder extends AbstractBuilder<GetterMethodBuilder> {

	private String fieldName;
	private String fieldType;
	private boolean cloneOnReturn;
	private JAccess access = JAccess.PUBLIC;
	
	public static GetterMethodBuilder builder(){
		return new GetterMethodBuilder();
	}
	
	public GetterMethodBuilder(){
		setPattern("bean.getter");
	}
	
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
	
	public GetterMethodBuilder setFromField(JField f) {
		setFieldName(f.getName());
		setFieldType(f.getTypeSignature());
		return this;
	}

	public GetterMethodBuilder setMethodAccess(JAccess access) {
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
