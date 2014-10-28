package org.codemucker.jmutate.ast;

import java.util.List;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

public class JAnnotation implements AstNodeProvider<Annotation>{

	private final Annotation annotation;

	public static boolean is(ASTNode node){
        return node instanceof Annotation;
    }
	
    public static JAnnotation from(ASTNode node) {
        if (node instanceof Annotation) {
            return from((Annotation) node);
        }
        throw new IllegalArgumentException(String.format("Expect a {0} but was {1}", Annotation.class.getName(), node.getClass().getName()));
    }
    
	public static JAnnotation from(Annotation node){
        return new JAnnotation(node);
    }
    
    private JAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }
    
    @Override
    public Annotation getAstNode() {
        return annotation;
    }

	public boolean isOfType(Class<? extends java.lang.annotation.Annotation> annotationClass) {
		return isOfType(JavaNameUtil.compiledNameToSourceName(annotationClass));
	}
	
	public boolean isOfType(String expectFqn) {
		return isOfType(AString.equalTo(expectFqn));
	}
	
	private boolean isOfType(Matcher<String> matcher) {
        return matcher.matches(getQualifiedName());
    }
    
	public String getQualifiedName() {
		return JavaNameUtil.resolveQualifiedName(annotation.getTypeName());
	}

	public String getValueForAttribute(String name){
		return getValueForAttribute(name,"");
	}
	
	public String getValueForAttribute(String name,String defaultValue){
		//TODO:handle nested annotations
		SimpleName val = null;
		if( annotation.isMarkerAnnotation()){
			val = null;
		}
		if( annotation instanceof SingleMemberAnnotation){
			if( "value".equals(name)){
				val = (SimpleName)((SingleMemberAnnotation)annotation).getValue();
			}	
		}
		if( annotation instanceof NormalAnnotation){
			NormalAnnotation normal = (NormalAnnotation)annotation;
			List<MemberValuePair> values = normal.values();
			for( MemberValuePair pair:values){
				if( name.equals(pair.getName().getIdentifier())){
					val = (SimpleName)pair.getValue();
					break;
				}
			}
		}
		
		return val==null?defaultValue:val.getIdentifier();
	}

    public final boolean equals(Object obj) {
        if(obj ==null || !(obj instanceof JAnnotation)){
            return false;
        }
        return annotation.equals(((JAnnotation)obj).annotation);
    }

    public final int hashCode() {
        return annotation.hashCode();
    }

    public final String toString() {
        return annotation.toString();
    }

	
}
