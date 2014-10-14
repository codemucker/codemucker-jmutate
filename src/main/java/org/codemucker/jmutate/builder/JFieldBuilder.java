package org.codemucker.jmutate.builder;

import static org.codemucker.lang.Check.checkNotNull;

import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.AstNodeFlattener;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.codemucker.jmutate.util.TypeUtil;
import org.codemucker.jpattern.Pattern;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;

/**
 * I build a {@link JField}
 */
public class JFieldBuilder extends AbstractBuilder<JFieldBuilder,JField> {

	public static enum RETURN {
		VOID, TARGET, ARG;
	}

	private String name;
	private String fieldType;
	private JAccess access = JAccess.PRIVATE;
	private Expression fieldValue;

	public static JFieldBuilder with(){
		return new JFieldBuilder();
	}
	
	public JFieldBuilder(){
		pattern("bean.property");
	}
	
	@Override
	public JField build(){
		checkFieldsSet();
		checkNotNull("name", name);
		checkNotNull("type", fieldType);

		return JField.from(toFieldNode());
	}
	
	private FieldDeclaration toFieldNode(){
		SourceTemplate t = getContext().newSourceTemplate()
			.setVar("fieldName", name)
			.setVar("fieldType", fieldType);

		if(isMarkedGenerated()){
			t.p('@')
			.p(Pattern.class.getName())
			.p("(name=\"")
			.p(getPattern())
			.pl("\")");
		}
		t.print(access.toCode());
		t.print(" ${fieldType} ${fieldName}");
		if( fieldValue != null){
			String valAsString = getContext().obtain(AstNodeFlattener.class).flatten(fieldValue);
			t.setVar("value", valAsString);
			t.pl(" = ${value};");
		} else {
			t.pl(";");
		}
	
		return t.asResolvedFieldNode();
	}

	public JFieldBuilder fieldAccess(JAccess access) {
    	this.access  = access;
    	return this;
    }

	public JFieldBuilder fieldName(String name) {
		this.name = name;
		return this;
	}

	public JFieldBuilder fieldType(Type type) {
		fieldType(JavaNameUtil.resolveQualifiedName(type));
		return this;
	}
	
	public JFieldBuilder fieldType(String type) {
		this.fieldType = TypeUtil.toShortNameIfDefaultImport(type);
		return this;
	}

	public JFieldBuilder fieldValue(String value) {
		this.fieldValue = getContext()
			.newSourceTemplate()
			.p(value)
			.asExpressionNode();
		return this;
	}
	
	public JFieldBuilder fieldValue(Expression expression) {
		this.fieldValue = expression;
		return this;
	}
}
