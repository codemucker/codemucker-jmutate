package org.codemucker.jmutate.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.util.List;

import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.matcher.AJAnnotationNode;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;

import com.google.common.base.Function;

public class JMethod implements AnnotationsProvider, AstNodeProvider<MethodDeclaration> {

	private static final Function<MethodDeclaration, JMethod> TRANSFORMER = new Function<MethodDeclaration, JMethod>() {
		public JMethod apply(MethodDeclaration node){
			return JMethod.from(node);
		}
	};

	private final MethodDeclaration methodNode;

    private final AbstractAnnotations annotable = new AbstractAnnotations() {
        @Override
        protected ASTNode getAstNode() {
            return methodNode;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<IExtendedModifier> getModifiers() {
            return methodNode.modifiers();
        }
    };
	  
    public static boolean isMethodNode(ASTNode node){
        return node instanceof MethodDeclaration;
    }
    
    public static Function<MethodDeclaration, JMethod> transformer(){
        return TRANSFORMER;
    }
    
	public static JMethod from(ASTNode node){
		if(node instanceof MethodDeclaration){
			return from((MethodDeclaration)node);
		}
		throw new IllegalArgumentException(String.format("Expect a {0} but was {1}",
			MethodDeclaration.class.getName(),
			node.getClass().getName()
		));
	}
	
	public static JMethod from(MethodDeclaration node){
		return new JMethod(node);
	}
	
	private JMethod(MethodDeclaration methodNode) {
		checkNotNull(methodNode, "expect java method node");

		this.methodNode = methodNode;
	}

	@Override
    public Annotations getAnnotations(){
        return annotable;
    }
    
	public JType getEnclosingJType(){
		return JType.from(getEnclosingType());
	}
	
	public AbstractTypeDeclaration getEnclosingType(){
		ASTNode node = getAstNode();
		while( node != null ){
			if(node instanceof AbstractTypeDeclaration){
				return (AbstractTypeDeclaration)node;
			}
			node = node.getParent();
		}
		throw new JMutateException("Couldn't find parent type. Unexpected");
	}
	
	public CompilationUnit getCompilationUnit(){
		ASTNode node = getAstNode();
		while( node != null ){
			if(node instanceof CompilationUnit){
				return (CompilationUnit)node;
			}
			node = node.getParent();
		}
		throw new JMutateException("Couldn't find compilation unit. Unexpected");
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
	
	public String getReturnTypeFullName(){
		if( isVoid()){
			return "void";
		}
		return JavaNameUtil.resolveQualifiedNameElseShort(methodNode.getReturnType2());
	}

	public boolean isVoid(){
		return methodNode.getReturnType2() == null || "void".equals(methodNode.getReturnType2().toString());
	}
	
	public boolean isStatic(){
		return getModifiers().isStatic();
	}
	
	public boolean isPublic(){
		return getModifiers().isPublic();
	}
	
	public boolean isPrivate(){
		return getModifiers().isPrivate();
	}
	
	public boolean isPackage(){
		return getModifiers().isPackagePrivate();
	}
	
	public boolean isAbstract(){
		return getModifiers().isAbstract();
	}
	
	@SuppressWarnings("unchecked")
    public JModifier getModifiers(){
		return new JModifier(methodNode.getAST(),methodNode.modifiers());
	}

    public <A extends Annotation> boolean hasParameterAnnotationOfType(Class<A> annotationClass) {
		return hasParameterAnnotation(AJAnnotationNode.with().fullName(annotationClass));
	}
	
	@SuppressWarnings("unchecked")
    public boolean hasParameterAnnotation(final Matcher<JAnnotation> matcher) {
	    ParamAnnotations paramAnons = new ParamAnnotations();
        List<SingleVariableDeclaration> parameters = methodNode.parameters();
        for( SingleVariableDeclaration param:parameters){
            paramAnons.var = param;
            if(paramAnons.contains(matcher)){
                return true;
            }
        }
        return false;
    }
	
	private static class ParamAnnotations extends AbstractAnnotations {
	    SingleVariableDeclaration var;
	    
        @Override
        protected ASTNode getAstNode() {
            return var;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<IExtendedModifier> getModifiers() {
            return var.modifiers();
        }
	};
	
	public String getFullSignature() {
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
			JavaNameUtil.resolveQualifiedName(arg.getType(), sb);
		}
		sb.append(")");
	    return sb.toString();
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
	 *  Object foo(String bar,Collection&lt;String%gt; col) -- %gt; foo(java.lang.String,java.util.Collection)
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
		
		//TODO: could't we just use JavaNameUtil.GetQualifiedName(type) and then just strip out the generic bit? e.g. foo.Bar<string> -> foo.Bar
		if (t.isPrimitiveType()) {
			sb.append(((PrimitiveType) t).getPrimitiveTypeCode().toString());
		} else if (t.isSimpleType()) {
			SimpleType st = (SimpleType) t;
			String name = JavaNameUtil.resolveQualifiedNameElseShort(st.getName());
			int startOfGenericPart = name.indexOf('<');
			if( startOfGenericPart != -1){
				name = name.substring(0, startOfGenericPart);
			}
			sb.append(name);
		} else if (t.isQualifiedType()) {
			QualifiedType qt = (QualifiedType) t;
			sb.append(JavaNameUtil.resolveQualifiedName(qt.getName()));
		} else if (t.isArrayType()) {
			Type elementType = ((ArrayType) t).getElementType();
			int dimensions = ((ArrayType) t).getDimensions();
			toNonGenericFullName(elementType, sb);
			while(dimensions> 0){
			    sb.append("[]");
			    dimensions--;
			}
		} else if(t.isParameterizedType()){
			ParameterizedType pt = (ParameterizedType)t;
			toNonGenericFullName(pt.getType(),sb);
		} else {
			throw new JMutateException("Currently don't know how to handle type:" + t);
		}
	}

	@Override
	public String toString(){
		return getAstNode().toString();
	}

}
