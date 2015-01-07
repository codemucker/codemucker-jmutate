package org.codemucker.jmutate.ast;

import java.util.List;

import javax.print.DocFlavor.STRING;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.util.NameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;

public class JAnnotation implements AstNodeProvider<Annotation>{

    private static final Logger log = LogManager.getLogger(JAnnotation.class);
    
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
		return isOfType(NameUtil.compiledNameToSourceName(annotationClass));
	}
	
	public boolean isOfType(String expectFqn) {
		return isOfType(AString.equalTo(expectFqn));
	}
	
	private boolean isOfType(Matcher<String> matcher) {
        return matcher.matches(getQualifiedName());
    }
    
	public String getQualifiedName() {
		return NameUtil.resolveQualifiedName(annotation.getTypeName());
	}

	public String getValueAsStringForAttribute(String name){
		return getValueAsStringForAttribute(name,"");
	}
	
	public String getValueAsStringForAttribute(String name,String defaultValue){
		String val = null;
		Expression exp = getExpressionForAttributeOrNull(name);
		if(exp != null){
			val = extractExpressionValue(name,annotation,exp);
		}
		return val==null?defaultValue:val;
	}
	
	public Expression getExpressionForAttributeOrNull(String name){
		//TODO:handle nested annotations
		Expression val = null;
		if(annotation.isMarkerAnnotation()){
			val = null;
		}
		if(annotation instanceof SingleMemberAnnotation){
			if( "value".equals(name)){
			    val = ((SingleMemberAnnotation)annotation).getValue();
			}	
		} else if( annotation instanceof NormalAnnotation){
			NormalAnnotation normal = (NormalAnnotation)annotation;
			List<MemberValuePair> values = normal.values();
			for( MemberValuePair pair:values){
				if(name.equals(pair.getName().getIdentifier())){
				    val = pair.getValue();
					break;
				}
			}
		}
		
		return val;
	}
	
	
	private String extractExpressionValue(String name, Annotation annotation, Expression exp){
	    if(exp instanceof StringLiteral){
            return ((StringLiteral)exp).getLiteralValue();
	    } else if(exp instanceof SimpleName){
            return ((SimpleName)exp).getIdentifier();
	    } else if(exp instanceof QualifiedName){
            return ((QualifiedName)exp).getFullyQualifiedName();
        } else {
            log.warn("couldn't extract annotation value named '" + name + "' from expression " + exp.getClass() + ", node " + annotation);
            //throw new JMutateException("couldn't extract annotation value named '" + name + "' from node " + annotation);
            
        }
	    return null;
	}

	public JCompilationUnit getJCompilationUnit(){
		return JCompilationUnit.from(getCompilationUnit());
	}
	
	public CompilationUnit getCompilationUnit(){
		return JCompilationUnit.findCompilationUnit(annotation);
	}

    @Override
    public final boolean equals(Object obj) {
        if(obj ==null || !(obj instanceof JAnnotation)){
            return false;
        }
        return annotation.equals(((JAnnotation)obj).annotation);
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
