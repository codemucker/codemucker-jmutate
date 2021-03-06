package org.codemucker.jmutate.ast;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.SourceLoader;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class JAnnotation implements AstNodeProvider<Annotation> {

	private static final Logger LOG = LogManager.getLogger(JAnnotation.class);

	private final Annotation annotation;

	public static boolean is(ASTNode node) {
		return node instanceof Annotation;
	}

	public static JAnnotation from(ASTNode node) {
		if (node instanceof Annotation) {
			return from((Annotation) node);
		}
		throw new IllegalArgumentException(String.format(
				"Expect a {0} but was {1}", Annotation.class.getName(), node
						.getClass().getName()));
	}

	public static JAnnotation from(Annotation node) {
		return new JAnnotation(node);
	}

	private JAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	@Override
	public Annotation getAstNode() {
		return annotation;
	}

	public String getFullName() {
		return NameUtil.resolveQualifiedName(annotation.getTypeName());
	}

	public Object getAttributeValue(String name) {
		return getAttributeValue(name, "");
	}

	public Object getAttributeValue(String name, Object defaultValue) {
		Object val = null;
		Expression exp = getAttributeValueOrNull(name);
		if (exp != null) {
			val = extractExpressionValue(annotation, exp);
		}
		return val == null ? defaultValue : val;
	}

	/**
	 * Return the attributes as a map of name/values. Child annotations are
	 * converted into a map of attribute name/values (recursive)
	 * 
	 * @return
	 */
	public Map<String, Object> getAttributeMap() {
		Map<String, Object> values = new HashMap<>();
		// if(annotation.isMarkerAnnotation()){
		// //do nothing
		// }
		//
		if (annotation instanceof SingleMemberAnnotation) {
			Object val = ((SingleMemberAnnotation) annotation).getValue();
			values.put("value", val);
		} else if (annotation instanceof NormalAnnotation) {
			NormalAnnotation normal = (NormalAnnotation) annotation;
			List<MemberValuePair> valuePairs = normal.values();
			for (MemberValuePair pair : valuePairs) {
				String name = pair.getName().getIdentifier();
				Object val = extractExpressionValue(annotation,pair.getValue());
				values.put(name, val);
			}
		}
		return values;
	}

	public List<String> getAttributeNames() {
		List<String> names = new ArrayList<>();
		if (annotation.isMarkerAnnotation()) {
			// do nothing
		}
		if (annotation instanceof SingleMemberAnnotation) {
			names.add("value");
		} else if (annotation instanceof NormalAnnotation) {
			NormalAnnotation normal = (NormalAnnotation) annotation;
			List<MemberValuePair> values = normal.values();
			for (MemberValuePair pair : values) {
				names.add(pair.getName().getIdentifier());
			}
		}
		return names;
	}

	public Expression getAttributeValueOrNull(String name) {
		// TODO:handle nested annotations
		Expression val = null;
		if (annotation.isMarkerAnnotation()) {
			val = null;
		}
		if (annotation instanceof SingleMemberAnnotation) {
			if ("value".equals(name)) {
				val = ((SingleMemberAnnotation) annotation).getValue();
			}
		} else if (annotation instanceof NormalAnnotation) {
			NormalAnnotation normal = (NormalAnnotation) annotation;
			List<MemberValuePair> values = normal.values();
			for (MemberValuePair pair : values) {
				if (name.equals(pair.getName().getIdentifier())) {
					val = pair.getValue();
					break;
				}
			}
		}

		return val;
	}

	private Object extractExpressionValue(ASTNode node,Expression exp) {
		if (exp instanceof StringLiteral) {
			return ((StringLiteral) exp).getLiteralValue();
		} else if (exp instanceof NumberLiteral) {
			return parseNumberLiteral(((NumberLiteral) exp).getToken());
		} else if (exp instanceof NullLiteral) {
			return null;
		} else if (exp instanceof TypeLiteral) {
			return NameUtil.resolveQualifiedName(((TypeLiteral)exp).getType());
		} else if (exp instanceof BooleanLiteral) {
			return ((BooleanLiteral)exp).booleanValue();
		} else if (exp instanceof SimpleName) {
			return ((SimpleName) exp).getIdentifier();
		} else if (exp instanceof QualifiedName) {
			//could be field access?
			//com.mycompany.Foo.BAR
			String qual = NameUtil.resolveQualifiedNameOrNull(((QualifiedName) exp).getQualifier());
			String fieldName = ((QualifiedName) exp).getName().getIdentifier();			
			String fullName = qual + "." + fieldName;
			
			Object val = findFieldValue(node, fullName, qual, fieldName);
			return val==null?fullName:val;
		} else if (exp instanceof Annotation) {
			return JAnnotation.from(exp).getAttributeMap();
		} else if (exp instanceof FieldAccess) {
			FieldAccess fa = (FieldAccess) exp;
			Expression fieldExp = fa.getExpression();
			if (fieldExp instanceof Name) {
				// e.g Foo.Bar
				Name fieldExpName = (Name) exp;
				
				// com.mycompany.Foo
				String className = NameUtil.resolveQualifiedName(fieldExpName);
				// Bar
				String fieldName = fa.getName().toString();
				String fullName = className + "." + fieldName;
				Object val = findFieldValue(node, fullName, className, fieldName);
				return val==null?fullName:val;
			}
		} else {
			LOG.warn("couldn't extract annotation value from expression type " + exp.getClass().getName() + ", node "+ node);

		}
		return null;
	}
	
	private Object parseNumberLiteral(String s){
		try {
			boolean hasUnderscore = false;
			boolean isHex = false;
			for(int i = 0;i < s.length();i++){
				char c = s.charAt(i);
				if(c == '_'){
					hasUnderscore = true;
				} else if (c == '.'){
					return Double.parseDouble(clean(s,hasUnderscore));
				} else if (c == 'x' || c == 'X'){
					isHex = true;
				} else if(!Character.isDigit(c) && c != '+' && c != '-'){
					s = clean(s,hasUnderscore);
					switch(c){
					case 'f':
					case 'F':
						return Float.parseFloat(s);
					case 'd':
					case 'D':
						return Double.parseDouble(s);
					case 'l':
					case 'L':
					case 'x':
					case 'X':
						return Long.parseLong(s,isHex?16:10);
					case 's':
					case 'S':
						return Short.parseShort(s,isHex?16:10);
					case 'b':
					case 'B':
						if(i ==1 && s.charAt(0) == '0'){ //e.g. 0b1000_1011  - > binary
							return Integer.parseInt(clean(s, true),2);
						}
						return Long.parseLong(s,isHex?16:10);
					default:
						LOG.warn("unrecognized character '" + c + "' in number literal '" + s + "', not parsing");
						return s;
					}				
				}
			}
			return Integer.parseInt(clean(s,hasUnderscore),isHex?16:10);
		} catch(NumberFormatException e){
			LOG.warn("couldn't parse '" + s + "' as a number");
		}
		return s;
	}

	private String clean(String s, boolean removeUnderscore){
		if(removeUnderscore){
			StringBuilder sb = new StringBuilder();
			for( int i = 0; i < s.length();i++){
				char c = s.charAt(i);
				if(c != '_'){
					sb.append(c);
				}
			}
			return sb.toString();
		}
		return s;
	}
	private Object findFieldValue(ASTNode node, String fullNameOfClassAndField, String fullClassName,String fieldName) {
		SourceLoader sourceLoader = MutateUtil.getSourceLoaderOrFail(node);
		
		JType type = sourceLoader.loadTypeForClass(fullClassName);
		if(type != null){
			JField field = type.findFieldsMatching(AJField.with().name(fieldName).isStatic()).getFirstOrNull();
			if(field != null){
				List<VariableDeclarationFragment> frags = field.getAstNode().fragments();
				if(frags.size() == 1){
					VariableDeclarationFragment val = frags.get(0);
					Expression varExp = val.getInitializer();
					Object fieldVal = extractExpressionValue(field.getAstNode(),varExp);
					return fieldVal;
				} else {
					//can't extract value!
				}
			} else {
				//can't find field!
			}
		}	
		//try the compiled class
		Class<?> loadedClass = sourceLoader.getResourceLoader().loadClassOrNull(fullClassName);
		if (loadedClass != null) {
			// find field
			// TODO:and parent fields?
			try {
				Field f = loadedClass.getDeclaredField(fieldName);
				if (Modifier.isStatic(f.getModifiers())) {
					return f.get(null);
				}
			} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalArgumentException("Couldn't access " + fullNameOfClassAndField, e);
			}
		}
	
		return null;
	}

	public JCompilationUnit getJCompilationUnit() {
		return JCompilationUnit.findCompilationUnit(getAstNode());
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null || !(obj instanceof JAnnotation)) {
			return false;
		}
		return annotation.equals(((JAnnotation) obj).annotation);
	}

	@Override
	public final int hashCode() {
		return annotation.hashCode();
	}

	@Override
	public final String toString() {
		return annotation.toString();
	}

}
