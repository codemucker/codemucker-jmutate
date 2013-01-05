package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;

import com.bertvanbrakel.codemucker.util.JavaNameUtil;

public class JMethod implements JAnnotatable, AstNodeProvider<MethodDeclaration> {

	private final MethodDeclaration methodNode;

	public static JMethod from(MethodDeclaration methodNode){
		return new JMethod(methodNode);
	}
	
	private JMethod(MethodDeclaration methodNode) {
		checkNotNull(methodNode, "expect java method node");

		this.methodNode = methodNode;
	}

	public JType getJType(){
		return JType.from(getType());
	}
	
	public AbstractTypeDeclaration getType(){
		ASTNode node = getAstNode();
		while( node != null ){
			if(node instanceof AbstractTypeDeclaration){
				return (AbstractTypeDeclaration)node;
			}
			node = node.getParent();
		}
		throw new CodemuckerException("Couldn't find parent type. Unexpected");
	}
	
	public CompilationUnit getCompilationUnit(){
		ASTNode node = getAstNode();
		while( node != null ){
			if(node instanceof CompilationUnit){
				return (CompilationUnit)node;
			}
			node = node.getParent();
		}
		throw new CodemuckerException("Couldn't find compilation unit. Unexpected");
	}
	
	@Override
	public MethodDeclaration getAstNode(){
		return methodNode;
	}
	
	@SuppressWarnings("unchecked")
    public List<TypeParameter> getParameters(){
		return methodNode.typeParameters();
	}
	
	public String getName(){
		return methodNode.getName().getIdentifier();
	}

	public boolean isConstructor() {
	    return methodNode.isConstructor();
    }
	
	public boolean isVoid(){
		return methodNode.getReturnType2() == null || "void".equals(methodNode.getReturnType2().toString());
	}
	
	@SuppressWarnings("unchecked")
    public JModifiers getJavaModifiers(){
		return new JModifiers(methodNode.getAST(),methodNode.modifiers());
	}

	@SuppressWarnings("unchecked")
    public <A extends Annotation> boolean hasParameterAnnotationOfType(Class<A> annotationClass) {
        List<SingleVariableDeclaration> parameters = methodNode.parameters();
		for( SingleVariableDeclaration param:parameters){
			if( JAnnotation.hasAnnotation(annotationClass, param.modifiers())){
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public <A extends Annotation> boolean hasAnnotationOfType(Class<A> annotationClass) {
		return JAnnotation.hasAnnotation(annotationClass, methodNode.modifiers());
	}

	@Override
	public <A extends Annotation> JAnnotation getAnnotationOfType(Class<A> annotationClass) {
		return JAnnotation.getAnnotationOfType(methodNode, JAnnotation.DIRECT_DEPTH, annotationClass);
	}
	
	@Override
	public Collection<org.eclipse.jdt.core.dom.Annotation> getAnnotations(){
		return JAnnotation.findAnnotations(methodNode, JAnnotation.DIRECT_DEPTH);
	}

	/**
	 * Return the method signature including just the name and arguments, stripping out any 
	 * information which is not needed to detect a clash. Intended use is to generate a signature 
	 * which can be compared to other signatures to see if these methods would clash when added 
	 * to the same class. Signatures which don't match must be able to coexist, signatures which
	 * are the same must result in a compilation error if both coexist. Methods which take a generic
	 * collection for example will clash if they both share the same name and collection type, therefore 
	 * generics are ignored in the signature string.
	 * 
	 * <p><pre>
	 *  Object foo(String bar,int[] args) -- %gt; foo(java.lang.String,int[])
	 *  Object foo(String bar,int[][] args) -- %gt; foo(java.lang.String,int[][])
	 *  Object foo(String bar,Collection<String> col) -- %gt; foo(java.lang.String,java.util.Collection)
	 * </pre></p>
	 * @return the signature
	 */
	public String getClashDetectionSignature() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append("(");
		@SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> args = methodNode.parameters();
		boolean comma = false;
		for( SingleVariableDeclaration arg:args){
			if( comma){
				sb.append(',');
			}
			comma = true;
			//sb.append(org.eclipse.jdt.internal.core.util.Util.getSignature(arg.getType()));
			//String,int,Map,.......	
			toNonGenericFullName(arg.getType(),sb);
		}
		sb.append(")");
	    return sb.toString();
    }
	
	//TODO:fallback to simple name if not full path found???
	private void toNonGenericFullName(Type t, StringBuilder sb){
		if (t.isPrimitiveType()) {
			sb.append(((PrimitiveType) t).getPrimitiveTypeCode().toString());
		} else if (t.isSimpleType()) {
			SimpleType st = (SimpleType) t;
			sb.append(JavaNameUtil.getQualifiedNameElseShort(st.getName()));
		} else if (t.isQualifiedType()) {
			QualifiedType qt = (QualifiedType) t;
			sb.append(JavaNameUtil.getQualifiedName(qt.getName()));
		} else if (t.isArrayType()) {
			ArrayType at = (ArrayType) t;
			toNonGenericFullName(at.getComponentType(), sb);
			sb.append("[]");
		} else if(t.isParameterizedType()){
			ParameterizedType pt = (ParameterizedType)t;
			toNonGenericFullName(pt.getType(),sb);
		} else {
			throw new CodemuckerException("Currently don't know how to handle type:" + t);
		}
	}
	
//	@Override
//	public String toString(){
//		return Objects
//			.toStringHelper(JMethod.class)
//			.add("name", toClashDetectionSignature())
//			.toString();
//	}
	
	@Override
	public String toString(){
		return getAstNode().toString();
	}
}
